package controllers


import play.Logger
import play.api.libs.iteratee.{Concurrent, Enumerator, Iteratee}
import play.api.libs.json.{JsValue, JsString}
import service.CameraService

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._


object Application extends Controller {

  def index = Action.async { request =>
    scala.concurrent.Future {
      Ok(views.html.main("Alarm Service", request.host))
    }


  }


  def socket = WebSocket.using[JsValue] { request =>
    val in = Iteratee.foreach[JsValue](println).map { _ =>
      Logger.info("Disconnected")
    }
    val (out, channel) = Concurrent.broadcast[JsValue]
    CameraService.stream(channel)
    (in, out)
  }
}
