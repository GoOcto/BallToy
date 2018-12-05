package com.goocto.balltoy

class Vec2(x:Float,y:Float) {

    // we use Doubles internally, but want everything going in and out to be Floats

    var x = x
    var y = y

    fun length():Float {
        return Math.sqrt((x*x+y*y).toDouble()).toFloat()
    }

    fun normalized():Vec2 {
        var l = this.length()
        var x = this.x / l
        var y = this.y / l
        return Vec2(x,y)
    }

    fun dot(v:Vec2):Float {
        return x*v.x + y*v.y
    }

    fun orthogonal():Vec2 {
        return Vec2(-y,x)
    }


}



operator fun Vec2.unaryMinus() = Vec2(-x,-y)

operator fun Vec2.plus(v:Vec2) = Vec2(x+v.x,y+v.y)

operator fun Vec2.minus(v:Vec2) = Vec2(x-v.x,y-v.y)

operator fun Vec2.times(n:Float) = Vec2(x*n,y*n)



//operator fun Vec2.plusAssign(v:Vec2) = Vec2(x+v.x,y+v.y)

