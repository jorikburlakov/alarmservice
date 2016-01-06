package service

import java.util.Date

import com.firebase.client._
import play.Logger
import play.api.Play
import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json._

/**
 * Created by Georg on 1/4/2016.
 */
object CameraService {


  var fireBase = new Firebase(Play.current.configuration.getString("firebase.url").get)
  fireBase.authWithCustomToken(Play.current.configuration.getString("token").get, new NestFireBaseAuthListener(new LoggerAuthenticationListener))

  def stream(chanel: Channel[JsValue]) = {
    fireBase.child("devices").child("cameras").addChildEventListener(new WebSocketChildEventListener(chanel))
  }

  trait AuthenticationListener {
    def onAuthenticationSuccess(string: String)
    def onAuthenticationFailure(errorCode: Int)
  }


  class LoggerAuthenticationListener extends AuthenticationListener {
    def onAuthenticationSuccess(string: String): Unit = {
      Logger.info(string)
    }

    def onAuthenticationFailure(errorCode: Int): Unit = {
      Logger.error("error with code" + errorCode)
    }
  }

  private class NestFireBaseAuthListener extends Firebase.AuthResultHandler {
    private var mListener: AuthenticationListener = null

    def this(listener: AuthenticationListener) {
      this()
      mListener = listener
    }

    def onAuthenticated(arg0: AuthData) {
      mListener.onAuthenticationSuccess(arg0.toString)
    }

    def onAuthenticationError(arg0: FirebaseError) {
      mListener.onAuthenticationFailure(arg0.getCode)
    }
  }


  class WebSocketChildEventListener(chanel: Channel[JsValue]) extends ChildEventListener {


    override def onChildChanged(dataSnapshot: DataSnapshot, s: String): Unit = {
      var returns = JsObject(Seq())
      val iterator = dataSnapshot.getChildren.iterator()
      while (iterator.hasNext) {
        val value = iterator.next()
        if (value.getName.equals("name_long")) {
          returns = returns + ("name" -> JsString(value.getValue().toString))
        } else
        if (value.getName.equals("is_online")) {
          returns = returns + ("online" -> JsBoolean(value.getValue().toString.toBoolean))
        } else
        if (value.getName.equals("device_id")) {
          returns = returns + ("device" -> JsString(value.getValue().toString))
        }
        else
        if (value.getName.equals("web_url")) {
          returns = returns + ("url" -> JsString(value.getValue().toString))
        }
        else
        if (value.getName.equals("last_event")) {
          var event = JsObject(Seq())
          val iteratorEvent = value.getChildren.iterator()
          while (iteratorEvent.hasNext) {
            val valueEvent = iteratorEvent.next()
            if (valueEvent.getName.equals("end_time")) {
              val v = valueEvent.getValue(new Date().getClass)
              event = event + ("endTime" -> JsNumber(v.getTime))
            } else if (valueEvent.getName.equals("start_time")) {
              val v = valueEvent.getValue(new Date().getClass)
              event = event + ("startTime" -> JsNumber(v.getTime))
            } else if (valueEvent.getName.equals("has_motion")) {
              event = event + ("hasMotion" -> JsBoolean(valueEvent.getValue().toString.toBoolean))
            } else if (valueEvent.getName.equals("has_sound")) {
              event = event + ("hasSound " -> JsBoolean(valueEvent.getValue().toString.toBoolean))
            } else if (valueEvent.getName.equals("web_url")) {
              event = event + ("video" -> JsString(valueEvent.getValue().toString))
            }
          }
          returns = returns + ("event" -> event)
        }

      }
      chanel push (returns)
    }

    override def onCancelled(firebaseError: FirebaseError): Unit = {
      Logger.error(firebaseError.getMessage + " : " + firebaseError.getDetails)
    }

    override def onChildAdded(dataSnapshot: DataSnapshot, s: String): Unit = {
      Logger.info("Camera added: "+s)
    }

    override def onChildRemoved(dataSnapshot: DataSnapshot): Unit = {
      Logger.info("Camera removed: "+dataSnapshot.getName)
    }


    override def onChildMoved(dataSnapshot: DataSnapshot, s: String): Unit = {
      Logger.info("Camera priority changed: "+s)
    }
  }


}