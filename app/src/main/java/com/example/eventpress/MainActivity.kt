package com.example.eventpress

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hardcopy.eventpress.EventHolder
import com.hardcopy.eventpress.EventPress
import com.hardcopy.eventpress.eventbus.EventFlowControl
import com.hardcopy.eventpress.eventbus.EventScheduler
import com.hardcopy.eventpress.eventbus.EventTopic
import com.hardcopy.eventpress.utils.DebugUtil
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Demo activity with EventPress test cases
 *
 * Created by godstale@hotmail.com(Suh Young-bae) on 2020-06-15.
 */

class MainActivity : AppCompatActivity() {
    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("###", "EventPress initializing....")
        EventPress.initialize()
        DebugUtil.setDebugMode(true)
        DebugUtil.useTopicDump(true)
    }

    override fun onStart() {
        super.onStart()

//        Log.d("###", "=====[Simple test]=====================================")
//        simpleTest()

//        Log.d("###", "=====[Basic test]======================================")
//        basicTest()

//        Log.d("###", "=====[Publish test]======================================")
//        publishTest()

//        Log.d("###", "=====[Event holder test]======================================")
//        eventHolderTest()

//        Log.d("###", "=====[Type casting error test]======================================")
//        typeCastingErrorTest()

//        Log.d("###", "=====[Remove topic test]======================================")
//        removeTopicTest()

//        Log.d("###", "=====[Builder test]======================================")
//        builderTest()

        Log.d("###", "=====[Flow control test]======================================")
        flowControlTest()
    }

    fun simpleTest() {
        EventPress.observe<String> {
            Log.d("###", "  -->[/sys/common] event received = $it")
        }?.addTo(compositeDisposable)

        Log.d("###", "[/sys/common] Publish message")
        EventPress.publish("Hello world!!")
    }

    fun basicTest() {
        val TOPIC_TEST_BASIC = "/test/basic"

        EventPress.builder()
            .setTopic(TOPIC_TEST_BASIC)
            .build()

        EventPress.observe<String>(TOPIC_TEST_BASIC) {
            Log.d("###", "  -->[$TOPIC_TEST_BASIC, #1] $it")
        }?.addTo(compositeDisposable)

        EventPress.getTopicFlowable<String>(TOPIC_TEST_BASIC)
            .subscribe {
                Log.d("###", "  -->[$TOPIC_TEST_BASIC, #2] $it")
            }?.addTo(compositeDisposable)

        Log.d("###", "[$TOPIC_TEST_BASIC] Publish message")
        EventPress.publish(TOPIC_TEST_BASIC, "Hello world!!", false)
    }

    fun publishTest() {
        val TOPIC_TEST_PUBLISH = "/test/pub"
        val TOPIC_TEST_PUBLISH1 = "/test/pub/depth1"
        val TOPIC_TEST_PUBLISH2A = "/test/pub/depth1/depth2a"
        val TOPIC_TEST_PUBLISH2B = "/test/pub/depth1/depth2b"

        // create topic [/test/pub] and observe
        EventPress.observe<String>(TOPIC_TEST_PUBLISH) {
            Log.d("###", "  -->[$TOPIC_TEST_PUBLISH] $it")
        }?.addTo(compositeDisposable)

        // create topic [/test/pub/depth1] and observe
        EventPress.observe<String>("$TOPIC_TEST_PUBLISH1") {
            Log.d("###", "  -->[$TOPIC_TEST_PUBLISH1] $it")
        }?.addTo(compositeDisposable)

        // create topic [/test/pub/depth1/depth2a] and observe
        EventPress.observe<String>("$TOPIC_TEST_PUBLISH2A") {
            Log.d("###", "  -->[$TOPIC_TEST_PUBLISH2A] $it")
        }?.addTo(compositeDisposable)

        // create topic [/test/pub/depth1/depth2b] and observe
        EventPress.observe<String>("$TOPIC_TEST_PUBLISH2B") {
            Log.d("###", "  -->[$TOPIC_TEST_PUBLISH2B] $it")
        }?.addTo(compositeDisposable)

        Log.d("###", "")
        Log.d("###", "[$TOPIC_TEST_PUBLISH] Publish message")
        EventPress.publish(TOPIC_TEST_PUBLISH, "Hello world!! (Single)", false)
        Log.d("###", " ")

        Log.d("###", "")
        Log.d("###", "[$TOPIC_TEST_PUBLISH] Publish message (Recursive)")
        EventPress.publish(TOPIC_TEST_PUBLISH, "Hello world!!")
    }

    fun removeTopicTest() {
        val TOPIC_TEST_REMOVE = "/test/pub"
        val TOPIC_TEST_REMOVE1 = "/test/pub/depth1"
        val TOPIC_TEST_REMOVE2A = "/test/pub/depth1/depth2a"
        val TOPIC_TEST_REMOVE2B = "/test/pub/depth1/depth2b"

        // create topic [/test/pub] and observe
        EventPress.observe<String>(TOPIC_TEST_REMOVE) {
            Log.d("###", "  -->[$TOPIC_TEST_REMOVE] $it")
        }?.addTo(compositeDisposable)

        // create topic [/test/pub/depth1] and observe
        EventPress.observe<String>("$TOPIC_TEST_REMOVE1") {
            Log.d("###", "  -->[$TOPIC_TEST_REMOVE1] $it")
        }?.addTo(compositeDisposable)

        // create topic [/test/pub/depth1/depth2a] and observe
        EventPress.observe<String>("$TOPIC_TEST_REMOVE2A") {
            Log.d("###", "  -->[$TOPIC_TEST_REMOVE2A] $it")
        }?.addTo(compositeDisposable)

        // create topic [/test/pub/depth1/depth2b] and observe
        val disposable2b = EventPress.observe<String>("$TOPIC_TEST_REMOVE2B") {
            Log.d("###", "  -->[$TOPIC_TEST_REMOVE2B] $it")
        }

        Log.d("###", "")
        Log.d("###", "[$TOPIC_TEST_REMOVE] Publish message (Recursive)")
        EventPress.publish(TOPIC_TEST_REMOVE, "Hello world!!")

        Log.d("###", "")
        Log.d("###", "[$TOPIC_TEST_REMOVE2B] Remove topic")
        EventPress.remove(TOPIC_TEST_REMOVE2B)
        Log.d("###", "[$TOPIC_TEST_REMOVE2B] Disposed = ${disposable2b?.isDisposed}")

        Log.d("###", "")
        Log.d("###", "[$TOPIC_TEST_REMOVE] Publish message (Recursive)")
        EventPress.publish(TOPIC_TEST_REMOVE, "Hello world!!")

        Log.d("###", "")
        Log.d("###", "[$TOPIC_TEST_REMOVE] Remove topic")
        EventPress.remove(TOPIC_TEST_REMOVE)
        Log.d("###", "[$TOPIC_TEST_REMOVE] Publish message (Recursive)")
        EventPress.publish(TOPIC_TEST_REMOVE, "Hello world!!")
    }

    fun builderTest() {
        val TOPIC_TEST_BUILDER = "/test/builder/default"

        /**
         * EventPressBuilder uses computation thread as default.
         */
        EventPress.builder()
            .setTopic(TOPIC_TEST_BUILDER)
            .build()

        // WARNING: this observer has critical problem (touch UI in backgroun thread)
        EventPress.observe<String>(TOPIC_TEST_BUILDER) {
            Log.d("###", "  -->[$TOPIC_TEST_BUILDER] $it")
            findViewById<TextView>(R.id.textMain).text = it
        }?.addTo(compositeDisposable)

        /**
         * If you want to receive event in UI thread,
         * use withScheduler(EventScheduler.Type.UI)
         */
        EventPress.builder()
            .setTopic(MainActivity::class.java)
            .withScheduler(EventScheduler.Type.UI)
            .build()

        EventPress.observe<String>(MainActivity::class.java) {
            Log.d("###", "  -->[UI #1] $it")
            findViewById<TextView>(R.id.textMain).text = it
        }?.addTo(compositeDisposable)

        EventPress.observe<String>(MainActivity::class.java) {
            Log.d("###", "  -->[UI #2] $it")
            findViewById<TextView>(R.id.textSub).text = it
        }?.addTo(compositeDisposable)

        EventPress.getTopicFlowable<String>(MainActivity::class.java)
            .subscribe {
                Log.d("###", "  -->[UI #3] $it")
                findViewById<TextView>(R.id.textSub2).text = it
            }?.addTo(compositeDisposable)

        /**
         * Send messages in background thread
         */
        Observable.intervalRange(1, 10, 0, 1000, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .subscribe({
                //Log.d("###", "[$TOPIC_TEST_BUILDER] Publish message")
                //EventPress.publish(TOPIC_TEST_BUILDER, it, false)

                Log.d("###", "[${EventTopic.getClassTopicString(MainActivity::class.java)}] Publish message")
                EventPress.publish(MainActivity::class.java,
                    "Hello world. count = $it")
            }, {}).addTo(compositeDisposable)
    }

    fun flowControlTest() {
        EventPress.builder()
            .setTopic(MainActivity::class.java)
            .withScheduler(EventScheduler.Type.UI)
            .withBackpressure(EventFlowControl.BpType.BUFFER)
            .withValve()
            .build()

        EventPress.observe<String>(MainActivity::class.java) {
            Log.d("###", "  -->[UI #1] $it")
            findViewById<TextView>(R.id.textMain).text = it
        }?.addTo(compositeDisposable)

        EventPress.observe<String>(MainActivity::class.java) {
            Log.d("###", "  -->[UI #2] $it")
            findViewById<TextView>(R.id.textSub).text = it
        }?.addTo(compositeDisposable)

        EventPress.getTopicFlowable<String>(MainActivity::class.java)
            .subscribe {
                Log.d("###", "  -->[UI #3] $it")
                findViewById<TextView>(R.id.textSub2).text = it
            }?.addTo(compositeDisposable)

        /**
         * Send messages in background thread
         */
        Observable.intervalRange(1, 100000, 0, 1, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .subscribe({
                EventPress.publish(MainActivity::class.java,
                    "Hello world. count = $it")
            }, {}).addTo(compositeDisposable)

        var valve = false
        Observable.intervalRange(1, 100, 1000, 5000, TimeUnit.MILLISECONDS)
            .observeOn(Schedulers.io())
            .subscribe {
                EventPress.switchTopicValve(MainActivity::class.java, valve)
                valve = !valve
            }.addTo(compositeDisposable)
    }

    fun eventHolderTest() {
        val TOPIC_TEST_EVENTHOLDER = "/test/eventholder"
        val eventType = 1

        EventPress.observe<EventHolder<String>>(TOPIC_TEST_EVENTHOLDER) {
            Log.d("###", "  -->[$TOPIC_TEST_EVENTHOLDER, #1] event = ${it.getEventType()}, ${it.getContentIfNotHandled()}")
        }?.addTo(compositeDisposable)

        EventPress.observe<EventHolder<String>>(TOPIC_TEST_EVENTHOLDER) {
            Log.d("###", "  -->[$TOPIC_TEST_EVENTHOLDER, #2] event = ${it.getEventType()}, ${it.getContentIfNotHandled()}")
        }?.addTo(compositeDisposable)

        EventPress.observe<EventHolder<String>>(TOPIC_TEST_EVENTHOLDER) {
            Log.d("###", "  -->[$TOPIC_TEST_EVENTHOLDER, #3] event = ${it.getEventType()}, ${it.getContentIfNotHandled()}")
        }?.addTo(compositeDisposable)

        Log.d("###", "[$TOPIC_TEST_EVENTHOLDER] Publish message")
        val eventHolder = EventHolder<String>(eventType, "Hello world!!")
        EventPress.publish(TOPIC_TEST_EVENTHOLDER, eventHolder, false)
    }

    fun typeCastingErrorTest() {
        val TOPIC_TEST_TYPECASTING = "/test/typecasting"
        val eventType = 1

        EventPress.observe<String>(TOPIC_TEST_TYPECASTING) {
            Log.d("###", "  -->[$TOPIC_TEST_TYPECASTING, #1] $it")
        }?.addTo(compositeDisposable)

        EventPress.observe<String>(TOPIC_TEST_TYPECASTING, {
            Log.d("###", "  -->[$TOPIC_TEST_TYPECASTING, #2] $it")
        }, {
            Log.d("###", "  -->[$TOPIC_TEST_TYPECASTING, #2] Error = ${it.toString()}")
        })?.addTo(compositeDisposable)?:

        Log.d("###", "[$TOPIC_TEST_TYPECASTING] Publish message")
        val eventHolder = EventHolder<String>(eventType, "Hello world!!")
        EventPress.publish(TOPIC_TEST_TYPECASTING, eventHolder, false)
    }

    fun makeTopicTest() {

    }

    fun connectToRxStreamTest() {

    }




    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.dispose()
    }



}