내용에 별 차이 없으니 한글이 편하시다면 여기로... [EventPress 한글 매뉴얼](http://roasterhouse.com/?p=12405)

### EventPress: Topic based event bus for Android [![](https://jitpack.io/v/godstale/EventPress.svg)](https://jitpack.io/#godstale/EventPress)

EventPress provides reactive style event bus, especially focused on effective management of multiple event streams. To achieve this EventPress implements topic management which is very similar with MQTT topic.

EventPress uses RxKotlin, RxAndroid, RxJava2-extensions and PublishProcessor for the event bus core. And inspired by one of good event bus implementation, [MFlisar's RxBus2](https://github.com/MFlisar/RxBus2 "MFlisar's RxBus2").

### Feature list

* Manage multiple event streams with topic hierarchy like MQTT protocol does.
* Each event stream you make has it's own topic path like "/viewmodel/logic/stream1"
* You can do the "make", "observe" or "publish" actions on a topic and it affects target topic and descendants.
* EventPress provides APIs for the backpressure strategy, valve control and scheduler of observer thread.  
* it's very **lightweight**
 
### Gradle settings (via [JitPack.io](https://jitpack.io/))

1. In your project level **build.gradle**:
```groovy
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```
2. Add the dependency at module level `build.gradle`: curret latest version is **1.0.0**
```groovy
dependencies {
	implementation 'com.github.godstale:EventPress:<LATEST-VERSION>'
}
```

### How to use

*Content*

- [Topic](#topic)
- [Test code](#test-code)
- [Simple usage](#simple-usage)
- [Basic usage](#basic-usage)
- [Publish an event](#publish-an-event)
- [Remove topics](#remove-topics)
- [Builder test](#builder-test)
- [Flow control](#flow-control)
- [Event holder](#event-holder)
- [Finally](#finally)


##### Topic

Topic is the hierarchical expression of each event stream which uses '/' as divider like path of file system. For example:

* /api
* /api/member
* /api/member/login
* /api/member/info

If you made topic hierarchy like example, publishing an event on the topic */api/member* delivers event to */api/member*, */api/member/logic* and */api/member/info* also.

You can name each event stream as you wish. But you have to follow below rules.

* Topic string must starts with '/'
* Topic string should not end with '/'
* Do not use root '/' expression at API call
* No blank is allowed
* Only [ . _ 0-9 a-z A-Z / - ] is allowed
* Max length is 256
* each topic name has one or more characters.
* /sys, /sys/class, /sys/ui and /sys/common is reserved. Do not use these topics on API call.
* Doesn't support wild card character.

##### Test code

Check out the [MainActivity](https://github.com/godstale/EventPress/blob/master/app/src/main/java/com/example/eventpress/MainActivity.kt), there are test codes to check out basic usage of EventPress.

##### Simple usage

Before calling the EventPress APIs, initialize EventPress first. (Application class is good to do this) :

    EventPress.initialize()

Most simpe way to observe and publish events. :

    // Register observer
    EventPress.observe<String> {
    	Log.d("###", "  -->[/sys/common] event received = $it")
    }?.addTo(compositeDisposable)
    
    // Send message
    EventPress.publish("Hello world!!")

No topic definition is found in this example but EventPress core uses common topic, **/sys/common** as default.

Very easy to use but keep in mind that all the observer on this topic must use same object type over the application. (See the type casting of **String**)

**And do not forget** the disposable.dispose() after use. Example used compositeDisposable to do this at finalize stage of activity class.

##### Basic usage

Observe and publish events on custom topic. :

    val TOPIC_TEST_BASIC = "/test/basic"

	// make topic
    EventPress.builder()
        .setTopic(TOPIC_TEST_BASIC)
        .build()

	// Observer 1
    EventPress.observe<String>(TOPIC_TEST_BASIC) {
        Log.d("###", "  -->[$TOPIC_TEST_BASIC, #1] $it")
    }?.addTo(compositeDisposable)

	// Observer 2
    EventPress.getTopicFlowable<String>(TOPIC_TEST_BASIC)
        .subscribe {
            Log.d("###", "  -->[$TOPIC_TEST_BASIC, #2] $it")
        }?.addTo(compositeDisposable)

	// Send an event
    EventPress.publish(TOPIC_TEST_BASIC, "Hello world!!", false)

You can make a topic, event stream, with EventPressBuilder. But without builder code, you can receive events by calling **EventPress.observe<>()**. Because EventPress makes topic automatically if it's not exist.    

There are two way of observation. **EventPress.observe<>()** attach your lambda function to **FLOWABLE** event stream.(internally calls Flowable.subscribe(your_lambda())) You cannot add to Rx operators on this stream but this way is safe from unexpectable exceptions.

**EventPress.getTopicFlowable<>()** exposes Flowable event stream to you directly. So you have to call **.subscribe()** by youself. You can add Rx operators on event stream but this could make unexpectable effects on all other observers in this event stream.   

##### Publish an event

Publish an event to topics. :

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

    // send an event to a targeted topic only
    EventPress.publish(TOPIC_TEST_PUBLISH, "Hello world!! (Single)", false)

    // send and event to a topic and descendants
    EventPress.publish(TOPIC_TEST_PUBLISH, "Hello world!!")

**recursive = false** parameter targets only one topic.

##### Remove topics

Test to remove a topic and descendants :

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

    // publish test
    EventPress.publish(TOPIC_TEST_REMOVE, "Hello world!!")

    // check topic is alive
    Log.d("###", "[$TOPIC_TEST_REMOVE2B] Disposed = ${disposable2b?.isDisposed}")

    // remove a topic
    EventPress.remove(TOPIC_TEST_REMOVE)

    // send an event and check removed or not
    EventPress.publish(TOPIC_TEST_REMOVE, "Hello world!!")

Always EventPress.remove() deletes target topic and descendants.

##### Builder test

How to use builder to apply scheduler to the topic :

    val TOPIC_TEST_BUILDER = "/test/builder/default"

    // EventPressBuilder uses computation thread as default.
    EventPress.builder()
        .setTopic(TOPIC_TEST_BUILDER)
        .build()

    // WARNING: this observer has a critical problem (touch UI in backgroun thread)
    EventPress.observe<String>(TOPIC_TEST_BUILDER) {
        Log.d("###", "  -->[$TOPIC_TEST_BUILDER] $it")
        findViewById<TextView>(R.id.textMain).text = it
    }?.addTo(compositeDisposable)

    // If you want to receive event in UI thread,
    // use withScheduler(EventScheduler.Type.UI)
    EventPress.builder()
        .setTopic(MainActivity::class.java)
        .withScheduler(EventScheduler.Type.UI)
        .build()

	// tests each observer runs in UI thread
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

    // Send messages in background thread
    Observable.intervalRange(1, 10, 0, 1000, TimeUnit.MILLISECONDS)
        .observeOn(Schedulers.io())
        .subscribe({
            EventPress.publish(MainActivity::class.java, "Hello world. count = $it")
        }, {}).addTo(compositeDisposable)

Add **.withScheduler(EventScheduler.Type.UI)** in builder method chain to run observer in UI thread.

##### Flow control

    // Make topic with backpressure strategy and valve control
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

    // Send messages in background thread at every 1ms for 100000 times
    Observable.intervalRange(1, 100000, 0, 1, TimeUnit.MILLISECONDS)
        .observeOn(Schedulers.io())
        .subscribe({
            EventPress.publish(MainActivity::class.java,
                "Hello world. count = $it")
        }, {}).addTo(compositeDisposable)

	// switch valve at every 5sec for 100 times
    var valve = false
    Observable.intervalRange(1, 100, 1000, 5000, TimeUnit.MILLISECONDS)
        .observeOn(Schedulers.io())
        .subscribe {
            EventPress.switchTopicValve(MainActivity::class.java, valve)
            valve = !valve
        }.addTo(compositeDisposable)

To enable backpressure strategy and valve control, add **.withBackpressure()** and **.withValve()** at topic build time. Backpressure strategy setting is passive over the topic stream's life time. And you can switch valve with **EventPress.switchTopicValve()**.

##### Event holder

EventPress.publish() delivers only one object. You have to use it wisely.

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

**EventPress.publish()** takes only one object. For the efficient usage of this object define your own object extends EventHolder. EventHolder follows event wrapper code suggested in below link.

[Event wrapper: the SingleLiveEvent case](https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150)


##### Finally

**EventPress.observe<>()** or **EventPress.getTopicFlowable<>().subscribe {}** returns disposable instance.

**Do not forget calling dispose() on disposable after use.** Observers keep alive until topic stream's end. Especially observers on system reserved topic(/sys/xxx) lasts until user terminates the app if you don't dispose it.

.

.
