package me.frmr.stripe

import net.liftweb.common._
import net.liftweb.json._
import net.liftweb.util._
import net.liftweb.util.Helpers._

import dispatch._, Defaults._
import com.ning.http.client.Response

/**
 * The base class for all singletons that will facilitate communication
 * with Stripe on behalf of the top-level model objects.
**/
trait StripeMeta {
  implicit val formats = DefaultFormats

  /**
   * This function should return the base resource URL for whatever kind
   * of resource this is.
  **/
  def baseResourceCalculator(request: Req): Req

  def metadataProcessor(metadata: Map[String, String]) = {
    metadata.map({
      case (key, value) =>
        (s"metadata[$key]", value)
    })
  }
}

trait Gettable[T <: StripeObject] extends StripeMeta {
  def get(id: String)(implicit exec: StripeExecutor, mf: Manifest[T]): Future[Box[T]] = {
    val getReq = baseResourceCalculator(exec.baseReq) / id
    exec.executeFor[T](getReq)
  }
}

abstract class Listable[Z <: StripeList[_]](implicit mf: Manifest[Z]) extends StripeMeta {
  def list(implicit exec: StripeExecutor): Future[Box[Z]] = {
    exec.executeFor[Z](baseResourceCalculator(exec.baseReq))
  }
}

trait Deleteable extends StripeMeta {
  def delete(id: String)(implicit exec: StripeExecutor): Future[Box[DeleteResponse]] = {
    val deleteReq = (baseResourceCalculator(exec.baseReq) / id).DELETE
    exec.executeFor[DeleteResponse](deleteReq)
  }
}

/**
 * The base class for all singletons that will facilitate communication
 * with Stripe on behalf of the child-level model objects.
**/
trait ChildStripeMeta[T <: StripeObject] {
  implicit val formats = DefaultFormats

  /**
   * This function should return the base resource URL for whatever kind
   * of resource this is.
  **/
  def baseResourceCalculator(request: Req, parentId: String): Req
}