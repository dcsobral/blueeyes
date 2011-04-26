package blueeyes.core.data

import blueeyes.json.JsonAST._
import blueeyes.json.Printer._
import blueeyes.json.JsonParser

import scala.xml.NodeSeq
import scala.xml.XML
import blueeyes.json.JsonParser.ParseException
import actors.Future

trait Bijection[T, S] { self =>
  def isDefinedAt(t: T): Boolean = {
    try {
      apply(t)
      
      true
    }
    catch {
      case _ => false
    }
  }
  
  def apply(t: T): S
  
  def unapply(s: S): T
  
  def inverse: Bijection[S, T] = new Bijection[S, T] {
    def apply(s: S): T   = self.unapply(s)
    def unapply(t: T): S = self.apply(t)
  }
  
  def compose[R](that: Bijection[R, T]): Bijection[R, S] = new Bijection[R, S] {
    def apply(r: R): S = self.apply(that.apply(r))
    
    def unapply(s: S): R = that.unapply(self.unapply(s))
  }
  
  def andThen[R](that: Bijection[S, R]): Bijection[T, R] = that.compose(self)
}

object Bijection {
  def identity[T]: Bijection[T, T] = new Bijection[T, T] {
    def apply(t: T): T = t
    
    def unapply(t: T): T = t
  }
}

trait BijectionsIdentity{
  implicit val JValueToJValue         = Bijection.identity[JValue]
  implicit val StringToString         = Bijection.identity[String]
  implicit val ArrayByteToArrayByte   = Bijection.identity[Array[Byte]]
  implicit val XMLToXML               = Bijection.identity[NodeSeq]
  implicit val ByteChunkToByteChunk   = Bijection.identity[ByteChunk]
}
object BijectionsIdentity extends BijectionsIdentity

trait BijectionsByteArray {
  import java.io.{InputStreamReader, ByteArrayInputStream, OutputStreamWriter, ByteArrayOutputStream}

  implicit val JValueToByteArray    = new Bijection[JValue, Array[Byte]]{
    def unapply(s: Array[Byte])  = JsonParser.parse(new InputStreamReader(new ByteArrayInputStream(s)))
    def apply(t: JValue)         = {
      val stream = new ByteArrayOutputStream()

      compact(render(t), new OutputStreamWriter(stream))

      stream.toByteArray()
    }
  }
  implicit val XMLToByteArray   = new Bijection[NodeSeq, Array[Byte]] {
    def apply(s: NodeSeq)       = s.toString.getBytes
    def unapply(t: Array[Byte]) = XML.load(new ByteArrayInputStream(t))
  }
  implicit val ByteArrayToString    = new Bijection[Array[Byte], String] {
    def apply(t: Array[Byte]): String   = new String(t)
    def unapply(s: String): Array[Byte] = s.getBytes
  }
  implicit val ByteArrayToJValue    = JValueToByteArray.inverse
  implicit val ByteArrayToXML       = XMLToByteArray.inverse
  implicit val StringToByteArray    = ByteArrayToString.inverse
}
object BijectionsByteArray extends BijectionsByteArray

trait BijectionsChunkJson{
  import java.io.{InputStreamReader, ByteArrayInputStream, OutputStreamWriter, ByteArrayOutputStream}

  implicit val JValueToChunk      = new Bijection[JValue, ByteChunk]{
    def apply(t: JValue)         = {
      val stream = new ByteArrayOutputStream()

      compact(render(t), new OutputStreamWriter(stream))

      new MemoryChunk(stream.toByteArray())
    }
    def unapply(s: ByteChunk)  = try {
      JsonParser.parse(new InputStreamReader(new ByteArrayInputStream(s.data)))
    }
    catch {
      case e: ParseException => error("Data is too big, use big data handler.")
    }
  }

  implicit val ChunkToJValue    = JValueToChunk.inverse
}
object BijectionsChunkJson extends BijectionsChunkJson

trait BijectionsChunkString {
  implicit val StringToChunk = new Bijection[String, ByteChunk] {
    def apply(s: String): ByteChunk   = new MemoryChunk(s.getBytes("UTF-8"))
    def unapply(t: ByteChunk): String = new String(t.data, "UTF-8")
  }

  implicit val ChunkToString    = StringToChunk.inverse
}
object BijectionsChunkString extends BijectionsChunkString

trait BijectionsChunkByteArray {
  implicit val ArrayByteToChunk = new Bijection[Array[Byte], ByteChunk] {
    def apply(t: Array[Byte]): ByteChunk    = new MemoryChunk(t)
    def unapply(s: ByteChunk): Array[Byte]  = s.data
  }

  implicit val ChunkToArrayByte = ArrayByteToChunk.inverse

  def chunksToChunksArrayByte[T](implicit bijection: Bijection[T, Array[Byte]]) = new Bijection[Chunk[T], ByteChunk]{
    def apply(t: Chunk[T])    = new ByteMemoryChunk(bijection(t.data), () => t.next.map(_.map(apply(_))))
    def unapply(s: ByteChunk) = new MemoryChunk[T](bijection.unapply(s.data), () => s.next.map(_.map(unapply(_))))
  }
}
object BijectionsChunkByteArray extends BijectionsChunkByteArray

trait BijectionsChunkXML {
  import java.io.{ByteArrayInputStream}
  implicit val XMLToChunk   = new Bijection[NodeSeq, ByteChunk] {
    def apply(s: NodeSeq)    = new MemoryChunk(s.toString.getBytes)
    def unapply(t: ByteChunk)    = try{
      XML.load(new ByteArrayInputStream(t.data))
    }
    catch {
      case e: org.xml.sax.SAXParseException => error("Data is too big, use big data handler.")
    }
  }

  implicit val ChunkToXML = XMLToChunk.inverse
}
object BijectionsChunkXML extends BijectionsChunkXML
