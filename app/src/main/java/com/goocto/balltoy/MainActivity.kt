package com.goocto.balltoy


import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager


class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var view:View
    lateinit var balls:List<Ball>

    // used for timing the frmarate
    var prevT = 0L
    var fps = 0

    // last time the vibrator was used
    var vibT = 0L

    val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

//    val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    val vibrator: Vibrator by lazy {
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        view = findViewById(R.id.container) as View
        balls = listOf(
            Ball(findViewById(R.id.ball0), view),
            Ball(findViewById(R.id.ball1), view),
            Ball(findViewById(R.id.ball2), view),
            Ball(findViewById(R.id.ball3), view),
            Ball(findViewById(R.id.ball4), view),
            Ball(findViewById(R.id.ball5), view),
            Ball(findViewById(R.id.ball6), view),
            Ball(findViewById(R.id.ball7), view),
            Ball(findViewById(R.id.ball8), view),
            Ball(findViewById(R.id.ball9), view),
            Ball(findViewById(R.id.ball10), view),
            Ball(findViewById(R.id.ball11), view)
        )

        // start all balls in a circle around the center of the screen
        for ( i in balls.indices ) {
            val angle = (2f * Math.PI) * i / balls.size
            val xpos = (  Math.cos(angle) * 120.0 ).toFloat()
            val ypos = ( -Math.sin(angle) * 120.0 ).toFloat()
            balls[i].offset(xpos,ypos)
        }

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

                // invert the x-axis to match screen coordinates
                val ax = -event.values[0]
                val ay =  event.values[1]
                val az =  event.values[2]

                animate(ax,ay,az)
            }
        }
    }

    fun bump(magnitude:Float) {
        var now = System.currentTimeMillis()
        if ( magnitude>2 && vibrator.hasVibrator() && now>vibT+51) {
            vibrator.cancel()
            //vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.DEFAULT_AMPLITUDE))
            vibT = now
        }
    }

    fun animate(ax:Float,ay:Float,az:Float) {

        val t = System.currentTimeMillis()

        if ( prevT>0 ) {

            val millis = t - prevT

            val thetaX = Math.atan2(ax.toDouble(),az.toDouble())
            val thetaY = Math.atan2(ay.toDouble(),az.toDouble())

            val gravX = Math.sqrt( (ax*ax + az*az).toDouble() ) // x-dir components of vertical force
            val gravY = Math.sqrt( (ay*ay + az*az).toDouble() ) // y-dir components of vertical force

            // A ball rolling down an inclined plane
            // ...the long way...
            //val m = 1f
            //val r = 1f
            //var inertia = (7f/5f)*(m*r*r)
            //var torqueX = gravX * r * Math.sin(thetaX)
            //var torqueY = gravY * r * Math.sin(thetaY)
            //var angularAccelX = torqueX / inertia
            //var angularAccelY = torqueY / inertia
            //var accelX = angularAccelX * r
            //var accelY = angularAccelY * r

            // or more simply... (mass and radius cancel out)
            var accelX = (5f/7f) * gravX * Math.sin(thetaX)
            var accelY = (5f/7f) * gravY * Math.sin(thetaY)

            // account for magnitude of timeslice (acceleration is in units per time squared)
            val k = .02f
            accelX *= k * millis*millis
            accelY *= k * millis*millis

            // accumulate bump force for tactile feedback
            // we only provide feedback for ball-wall interactions
            // presumably ball-ball interactions don't cause the device to `bump`
            var magBump = 0f
            for ( ball in balls ) magBump += ball.update(accelX.toFloat(),accelY.toFloat())
            bump(magBump)

            // each ball must interact with each other ball exactly once
            for ( i in 1..(balls.size-1) ) {   // all but the first
                for ( j in 0..(i-1) ) {        // from first up to but not including i'th
                    balls[i].collide(balls[j])
                }
            }
        }

        prevT = t
    }

}


