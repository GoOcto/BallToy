package com.goocto.balltoy

import android.view.View
import android.widget.ImageView
import java.util.*


class Ball(private val ball: ImageView, private val area: View) {

    var v = Vec2(0f,0f)
    var p = Vec2(ball.translationX,ball.translationY)

    fun offset( x:Float, y:Float ){
        p.x = x
        p.y = y
        ball.translationX = x
        ball.translationY = y
    }

    fun collide( that:Ball ) {

        // use of `this` is redundant here, but it makes the code look more consistent
        // with regards to this <-> that

        // calculate the vector between the two balls
        val vect = this.p - that.p
        val elas = 0.8f // elasticity of ball-ball interactions

        if ( vect.length()<ball.width ) {
            // a collision has occurred

            // component unit vectors, perpendicular to and parallel to collision direction
            val para = vect.normalized()
            val perp = para.orthogonal()

            // my vector in terms of components parallel and perpendicular to the collision direction
            val paraThis = perp * perp.dot(this.v)
            val perpThis = para * para.dot(this.v)

            // the that vector in terms of components parallel and perpendicular to the collision vector
            val paraThat = perp * perp.dot(that.v)
            val perpThat = para * para.dot(that.v)

            // transfer momentum by adjusting velocities
            this.v = paraThis + perpThis*(1f-elas) + perpThat*elas
            that.v = paraThat + perpThat*(1f-elas) + perpThis*elas

            // move each ball apart so they barely kiss
            val d = .5f*Math.abs((vect.length()-ball.width).toDouble() ).toFloat()
            this.p += para*d*1.001f
            that.p -= para*d*1.001f
        }
    }

    fun update( axi:Float, ayi:Float ):Float {

        val wid = 0.5f * (area.width  - ball.width ).toFloat()
        val hgt = 0.5f * (area.height - ball.height).toFloat()

        if ( wid==0f || hgt==0f ) return 0f

        // apply a tiny bit of variation to incoming accelerations
        val r = Random()
        val ax = axi * ( 1f + 0.01f*r.nextFloat() )
        val ay = ayi * ( 1f + 0.01f*r.nextFloat() )

        v = Vec2(ax,ay)*.05f + v
        p = p + v

        // accumulate each bump to provide tactile feedback
        var bump = 0f

        // balls bounce of edges with an amount of elasticity of ball-wall interactions
        if (p.x<-wid) {
            p.x = -wid
            v.x = -0.2f*v.x
            bump += v.x
        }
        if (p.x> wid) {
            p.x =  wid
            v.x = -0.2f*v.x
            bump -= v.x  // we need to change the sign, to make it positive
        }
        if (p.y<-hgt) {
            p.y = -hgt
            v.y = -0.8f*v.y // much bouncier at the top edge
            bump += v.y
        }
        if (p.y> hgt) {
            p.y =  hgt
            v.y = -0.2f*v.y
            bump -= v.y  // we need to change the sign, to make it positive
        }

        ball.translationX = p.x
        ball.translationY = p.y

        return bump;
    }


}
