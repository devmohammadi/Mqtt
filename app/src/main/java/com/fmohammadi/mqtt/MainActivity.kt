package com.fmohammadi.mqtt

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


class MainActivity : AppCompatActivity() {

    private lateinit var mqttClient: MqttAndroidClient

    companion object {
        const val TAG = "AndroidMqttClient"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connect.setOnClickListener {
            if (TextUtils.isEmpty(serverUrl.text.trim().toString())
                && TextUtils.isEmpty(username.text.trim().toString())
                && TextUtils.isEmpty(password.text.trim().toString())
            ) {
                Toast.makeText(this, "please enter information for connect", Toast.LENGTH_LONG)
                    .show()
            } else {
                connect(
                    this,
                    serverUrl.text.trim().toString(),
                    username.text.trim().toString(),
                    password.text.trim().toString()
                )
            }
        }

        sub.setOnClickListener {
            if (TextUtils.isEmpty(topic.text.trim().toString())) {
                Toast.makeText(this, "please enter topic", Toast.LENGTH_LONG).show()
            } else {
                subscribe(topic.text.trim().toString())
            }
        }

        pub.setOnClickListener {
            if (TextUtils.isEmpty(topic.text.trim().toString())) {
                Toast.makeText(this, "please enter topic and subscribe ", Toast.LENGTH_LONG).show()
            } else {
                if (TextUtils.isEmpty(message.text.trim().toString())) {
                    Toast.makeText(this, "please enter message then publish ", Toast.LENGTH_LONG)
                        .show()
                } else {
                    val m = "{HANDEL:\"switch\",PMQT:\"12345678\",TYP:\"power\"}"
                    publish(topic.text.trim().toString(), m)
                }
            }
        }

        disconnect.setOnClickListener {
            disconnect()
        }

        unsubscribed.setOnClickListener {
            unsubscribe(topic.text.trim().toString())
        }
    }


    // Connect MQTT broker
    private fun connect(context: Context, serverUrl: String, username: String, password: String) {
        // val serverUrl = "tcp://broker.emqx.io:1883"
        mqttClient = MqttAndroidClient(context, serverUrl, "kotlin_client")
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Receive message: ${message.toString()} from topic: $topic")
                tvMesseage.append("Receive message: ${message.toString()} from topic: $topic\n")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
                tvMesseage.append("Connection lost ${cause.toString()}\n")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        options.connectionTimeout = 5
        options.isAutomaticReconnect = true
        options.isCleanSession = true
        options.userName = username
        options.password = password.toCharArray()
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                    tvMesseage.setTextColor(Color.GREEN)
                    tvMesseage.append("Connection success\n")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure")
                    tvMesseage.setTextColor(Color.RED)
                    tvMesseage.append("Connection failure\n")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // Create MQTT subscription
    private fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                    tvMesseage.setTextColor(Color.BLUE)
                    tvMesseage.append("Subscribed to $topic\n")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
                    tvMesseage.setTextColor(Color.RED)
                    tvMesseage.append("Failed to subscribe $topic\n")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // Cancel subscription
    private fun unsubscribe(topic: String) {
        try {
            mqttClient.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Unsubscribed to $topic\n")
                    tvMesseage.setTextColor(Color.GREEN)
                    tvMesseage.append("Unsubscribed to $topic\n")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to unsubscribe $topic\n")
                    tvMesseage.setTextColor(Color.RED)
                    tvMesseage.append("Failed to unsubscribe $topic\n")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // Publish messages
    private fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttClient.publish("in00A59232", message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $topic")
                    tvMesseage.setTextColor(Color.BLUE)
                    tvMesseage.append("$msg published to $topic\n")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                    tvMesseage.setTextColor(Color.RED)
                    tvMesseage.append("Failed to publish $msg to $topic\n")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // Disconnect from the MQTT Broker
    private fun disconnect() {
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                    tvMesseage.setTextColor(Color.GREEN)
                    tvMesseage.append("Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                    tvMesseage.setTextColor(Color.RED)
                    tvMesseage.append("Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

}