package com.goocto.balltoy


import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager


class MainActivity : AppCompatActivity(), SensorEventListener {

    // accelerometer
    var ax = 0f
    var ay = 0f
    var az = 0f

    var init = false

    lateinit var view:View
    lateinit var balls:List<Ball>


    var prevT = 0L
    var fps = 0


    val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    //val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val vibratorService: Vibrator by lazy {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        view = findViewById(R.id.container) as View
        balls = listOf(
            Ball(findViewById(R.id.ball1), view),
            Ball(findViewById(R.id.ball2), view),
            Ball(findViewById(R.id.ball3), view)
        )

        balls[0].offset(-120f,-60f)
        balls[1].offset( -10f, 40f)
        balls[2].offset(  80f,-10f)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if ( hasFocus ) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        // for a good description of how these params affect the app
        // https://developer.android.com/training/system-ui/immersive
        val decorView = window.decorView
        decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    // Hide the nav bar and status bar
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }



    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_FASTEST
        )

        // retime the framerate
        prevT = 0
        fps = 0
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {

        when (event?.sensor?.type) {

            Sensor.TYPE_ACCELEROMETER -> {

                // invert the x-axis
                ax = -event.values[0]
                ay =  event.values[1]
                az =  event.values[2]

                animate(ax,ay,az)
            }

        }

    }

    fun animate(ax:Float,ay:Float,az:Float) {

        val t = System.currentTimeMillis()

        if ( prevT>0 ) {

            val millis = t - prevT

            val theta_x = Math.atan2(ax.toDouble(),az.toDouble())
            val theta_y = Math.atan2(ay.toDouble(),az.toDouble())

            val grav_x = Math.sqrt( (ax*ax + az*az).toDouble() ) // x-dir components of vertical force
            val grav_y = Math.sqrt( (ay*ay + az*az).toDouble() ) // y-dir components of vertical force

            // A ball rolling down an inclined plane
            // ...the long way...
            //val m = 1f
            //val r = 1f
            //var inertia = (7f/5f)*(m*r*r)
            //var torque_x = grav_x * r * Math.sin(theta_x)
            //var torque_y = grav_y * r * Math.sin(theta_y)
            //var angular_accel_x = torque_x / inertia
            //var angular_accel_y = torque_y / inertia
            //var linear_accel_x = angular_accel_x * r
            //var linear_accel_y = angular_accel_y * r

            // or more simply... (mass and radius cancel out)
            var accel_x = (5f/7f) * grav_x * Math.sin(theta_x)
            var accel_y = (5f/7f) * grav_y * Math.sin(theta_y)

            // account for magnitude of timeslice (acceleration is in units per time squared)
            val k = .02f
            accel_x *= k * millis*millis
            accel_y *= k * millis*millis

            for ( ball in balls ) ball.update(accel_x.toFloat(),accel_y.toFloat())

            // each ball must interact with each other ball exactly once
            balls[0].collide(balls[1])
            balls[0].collide(balls[2])
            balls[1].collide(balls[2])
        }

        prevT = t
    }

}


