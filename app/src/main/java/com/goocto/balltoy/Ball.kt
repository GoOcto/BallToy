package com.goocto.balltoy

import android.view.View
import android.widget.ImageView
import java.util.*

//import com.goocto.balltoy.R.drawable.ball

class Ball(private val ball: ImageView, private val area: View) {

    var v = Vec2(0f,0f)
    var p = Vec2(ball.translationX,ball.translationY)

    fun offset( x:Float, y:Float ){
        p.x = x
        p.y = y
        ball.translationX = x
        ball.translationY = y
    }

    fun collide( other:Ball ) {

        // calculate the vector between the two balls
        val n = p - other.p
        val elast = 0.8f

        if ( n.length()<ball.width ) {
            // a collision has occurred

            // component unit vectors, perpendicular to and parallel to collision direction
            val norm = n.normalized()
            val perp = norm.orthogonal()

            // my vector in terms of components parallel and perpendicular to the collision direction
            val parl_v = perp * perp.dot(v)
            val perp_v = norm * norm.dot(v)

            // the other vector in terms of components parallel and perpendicular to the collision vector
            val parl_o = perp * perp.dot(other.v)
            val perp_o = norm * norm.dot(other.v)

            // transfer momentum by adjusting velocities
            v       = parl_v + perp_v*(1f-elast) + perp_o*elast
            other.v = parl_o + perp_o*(1f-elast) + perp_v*elast

            // move each ball apart so they barely kiss
            val d = .5f*Math.abs((n.length()-ball.width).toDouble() ).toFloat()
            p       =       p + norm*d*1.001f
            other.p = other.p - norm*d*1.001f

        }
    }

    fun update( axi:Float, ayi:Float ){

        val wid = 0.5f * ((area.width).toFloat()  - ball.width)
        val hgt = 0.5f * ((area.height).toFloat() - ball.height)

        if ( wid==0f || hgt==0f ) return

        // apply a tiny bit of variation to incoming accelerations
        val r = Random()
        val ax = axi * ( 1f + 0.01f*r.nextFloat() )
        val ay = ayi * ( 1f + 0.01f*r.nextFloat() )

        v = Vec2(ax,ay)*.05f + v
        p = p + v

        if (p.x<-wid) { p.x = -wid; v.x = -0.2f*v.x }
        if (p.x> wid) { p.x =  wid; v.x = -0.2f*v.x }
        if (p.y<-hgt) { p.y = -hgt; v.y = -0.8f*v.y } // much bouncier at the top edge
        if (p.y> hgt) { p.y =  hgt; v.y = -0.2f*v.y }

        ball.translationX = p.x
        ball.translationY = p.y
    }


}
