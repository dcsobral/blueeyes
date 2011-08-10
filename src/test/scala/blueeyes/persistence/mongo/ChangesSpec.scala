package blueeyes.persistence.mongo

import org.specs.Specification
import blueeyes.json.JPath
import blueeyes.persistence.mongo.Changes.Change1
import collection.immutable.ListSet


class ChangesSpec extends Specification {
  "Changes.compose" should{
    "leave changes with diffrent paths" in{
      Changes.compose(ListSet.empty + ChangeImpl(JPath("bar"), 2, false), ListSet.empty + ChangeImpl(JPath("foo"), 2, false)) mustEqual(ListSet.empty ++ List(ChangeImpl(JPath("bar"), 2, false), ChangeImpl(JPath("foo"), 2, false)))
    }
    "fuse changes with the same paths" in{
      Changes.compose(ListSet.empty + ChangeImpl(JPath("foo"), 2, true), ListSet.empty + ChangeImpl(JPath("foo"), 1, true)) mustEqual(ListSet.empty + ChangeImpl(JPath("foo"), 1, true))
    }
    "fuse several changes" in{
      val listSet = Changes.compose(ListSet.empty ++ List(ChangeImpl(JPath("foo"), 2, true), ChangeImpl(JPath("bar"), 2, true)), ListSet.empty ++ List(ChangeImpl(JPath("foo"), 1, true), ChangeImpl(JPath("bar"), 1, true)))
      listSet mustEqual(ListSet.empty ++ List(ChangeImpl(JPath("foo"), 1, true), ChangeImpl(JPath("bar"), 1, true)))
    }
    "leave changes with the same paths which cannot be fused" in{
      Changes.compose(ListSet.empty + ChangeImpl(JPath("foo"), 2, false), ListSet.empty + ChangeImpl(JPath("foo"), 1, false)) mustEqual(ListSet.empty ++ List(ChangeImpl(JPath("foo"), 2, false), ChangeImpl(JPath("foo"), 1, false)))
    }
    "fuse only changes  which can be fused" in{
      val listSet = Changes.compose(ListSet.empty ++ List(ChangeImpl(JPath("foo"), 2, false), ChangeImpl(JPath("foo"), 2, true), ChangeImpl(JPath("bar"), 2, true)), ListSet.empty + ChangeImpl(JPath("foo"), 1, true))
      listSet mustEqual(ListSet.empty ++ List(ChangeImpl(JPath("foo"), 2, false), ChangeImpl(JPath("foo"), 1, true), ChangeImpl(JPath("bar"), 2, true)))
    }
  }
}

case class ChangeImpl(path: JPath, value: Int, canBeFused: Boolean) extends Changes.Change1{
  protected def fuseWithImpl(older: Change1) = if (canBeFused) Some(this) else None
}
