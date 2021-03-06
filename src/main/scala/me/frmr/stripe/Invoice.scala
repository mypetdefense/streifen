package me.frmr.stripe

import net.liftweb.common._
import net.liftweb.json._
  import JsonDSL._
  import Extraction._
import net.liftweb.util.Helpers._

import dispatch._, Defaults._

case class InvoiceLineItemPeriod(start: Long, end: Long)

case class InvoiceLineItem(
  id: String,
  livemode: Boolean,
  amount: Long,
  currency: String,
  period: Option[InvoiceLineItemPeriod],
  proration: Boolean,
  `type`: String,
  description: Option[String],
  plan: Option[Plan],
  quantity: Int,
  subscription: Option[String],
  metadata: Map[String, String]
)

case class Invoice(
  id: String,
  livemode: Boolean,
  amountDue: Long,
  attemptCount: Int,
  attempted: Boolean,
  closed: Boolean,
  currency: String,
  customer: String,
  date: Long,
  forgiven: Boolean,
  lines: InvoiceLineItemList,
  paid: Boolean,
  periodEnd: Long,
  periodStart: Long,
  startingBalance: Long,
  subtotal: Long,
  total: Long,
  applicationFee: Option[Long],
  charge: String,
  description: Option[String],
  discount: Option[Discount],
  endingBalance: Long,
  nextPaymentAttempt: Option[Long],
  receiptNumber: Option[String],
  statementDescriptor: String,
  subscription: Option[String],
  webhooksDeliveredAt: Long,
  tax: Long,
  taxPercent: Option[Double],
  metadata: Map[String, String],
  raw: Option[JValue] = None
) extends StripeObject {
  def withRaw(raw: JValue) = this.copy(raw = Some(raw))
}

object Invoice extends Listable[InvoiceList] with Gettable[Invoice] {
  def baseResourceCalculator(req: Req) =
    req / "invoices"

  def create(
    customer: String,
    applicationFee: Option[Long] = None,
    description: Option[String] = None,
    statementDescriptor: Option[String] = None,
    subscription: Option[String] = None,
    metadata: Map[String, String] = Map.empty
  )(implicit exec: StripeExecutor) = {
    val requiredParams = Map("customer" -> customer)

    val optionalParams = List(
      applicationFee.map(a => ("application_fee", a.toString)),
      description.map(("description", _)),
      statementDescriptor.map(("statement_descriptor", _)),
      subscription.map(("subscription", _))
    ).flatten.toMap

    val params = requiredParams ++ optionalParams ++ metadataProcessor(metadata)
    val uri = baseResourceCalculator(exec.baseReq)

    exec.executeFor[Invoice](uri << params)
  }

  def getLines(invoiceId: String)(implicit exec:StripeExecutor) = {
    val uri = baseResourceCalculator(exec.baseReq) / invoiceId / "lines"
    exec.executeFor[InvoiceLineItemList](uri)
  }

  def getUpcoming(
    customer: String,
    subscription: Option[String] = None
  )(implicit exec: StripeExecutor) = {
    val requiredParams = Map("customer" -> customer)
    val optionalParams = List(
      subscription.map(("subscription", _))
    ).flatten.toMap
    val params = requiredParams ++ optionalParams
    val uri = baseResourceCalculator(exec.baseReq) / "upcoming"

    exec.executeFor[Invoice](uri <<? params)
  }

  def update(
    id: String,
    applicationFee: Option[Long] = None,
    closed: Option[Boolean] = None,
    description: Option[String] = None,
    forgiven: Option[Boolean] = None,
    statementDescriptor: Option[String] = None,
    metadata: Map[String, String] = Map.empty
  )(implicit exec: StripeExecutor) = {
    val params = List(
      applicationFee.map(a => ("application_fee", a.toString)),
      closed.map(c => ("closed", c.toString)),
      description.map(("description", _)),
      forgiven.map(f => ("forgiven", f.toString)),
      statementDescriptor.map(("statement_descriptor", _))
    ).flatten.toMap
    val uri = baseResourceCalculator(exec.baseReq) / id

    exec.executeFor[Invoice](uri << params)
  }

  def pay(id: String)(implicit exec: StripeExecutor) = {
    val uri = baseResourceCalculator(exec.baseReq) / id / "pay"
    exec.executeFor[Invoice](uri.POST)
  }
}
