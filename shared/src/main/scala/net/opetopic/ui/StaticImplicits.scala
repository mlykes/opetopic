/**
  * StaticImplicits.scala - Implementations of iterated static renderings
  * 
  * @author Eric Finster
  * @version 0.1 
  */

package net.opetopic.ui

import net.opetopic.core._

object StaticImplicits {

  def complexIsRenderable[A, F <: UIFramework](implicit rn: Renderable[A, F]): Renderable[SComplex[A], F] =
    new Renderable[SComplex[A], F] {

      def render(frmwk: F)(cmplx: SComplex[A]): frmwk.CellRendering = {

        import frmwk._

        val gallery = new SimpleStaticGallery(frmwk)(cmplx)
        // CellRendering(gallery.boundedElement)

        ???

      }

    }

}
