package com.example.day9

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {
    private val dots: Array<ImageView> by lazy {
        arrayOf(sDot1, sDot2, sDot3, sDot4, sDot5, sDot6, sDot7, sDot8, sDot9)
    }

    private val barheight by lazy {
        //屏幕尺寸
        val display = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(display)

        val drawingRect = Rect()
        window.findViewById<ViewGroup>(Window.ID_ANDROID_CONTENT).getDrawingRect(drawingRect)

        display.heightPixels - drawingRect.height()
    }

    //将触摸点的坐标转换为容器的坐标
    private fun transform(event: MotionEvent): Point {
        return Point().apply {
            x = (event.x - mcontainer.x).toInt()
            y = (event.y - barheight - mcontainer.y).toInt()
        }
    }

    //获取触摸点所在的圆点视图
    private fun findViewThatContainsPoint(point: Point): ImageView? {
        //遍历所有的圆点视图，判断是否有包含该点的视图
        for (dotView in dots) {
            getRectForView(dotView).also {
                if (it.contains(point.x, point.y))
                    return dotView
            }
        }
        return null
    }

    //获取圆点视图的区域
    private fun getRectForView(v: ImageView) = Rect(v.left, v.top, v.right, v.bottom)

    //点亮圆点视图
    private fun highLightView(v: ImageView?) {
        if (v != null && v.visibility == View.INVISIBLE) {
            if (lastSelectedView == null) {
                //第一次点亮这个点
                highlightdot(v)
            } else {
                //获取上个点和这个点的tag值
                val previous = (lastSelectedView?.tag as String).toInt()
                val current = (v?.tag as String).toInt()
                val lineTag =
                    if (previous > current) current * 10 + previous else previous * 10 + current
                //判断是否有这条线
                if (allLineTags.contains(lineTag)) {
                    //点亮点
                    highlightdot(v)
                    //点亮线
                    mcontainer.findViewWithTag<ImageView>(lineTag.toString()).apply {
                        visibility = View.VISIBLE
                        allSelectedViews.add(this)
                    }
                }

            }


        }
    }


    private fun highlightdot(v: ImageView) {
        v.visibility = View.VISIBLE
        allSelectedViews.add(v)
        password.append(v.tag)
        lastSelectedView = v
    }

    //保存被点亮的视图
    private val allSelectedViews = mutableListOf<ImageView>()

    //记录滑动轨迹
    private val password = StringBuilder()

    //记录原始密码
    private var orgPassword: String? = null

    //记录第一次设置的密码
    private var firstPassword: String? = null

    private val RequestImageCode = 123
    private val RequestVideoCode = 124

    //保存线条的tag
    private val allLineTags = arrayOf(
        12, 23, 45, 56, 78, 89,
        14, 25, 36, 47, 58, 69,
        24, 35, 57, 68, 15, 26, 48, 59
    )

    //记录点亮的点的对象
    private var lastSelectedView: ImageView? = null


    //还原视图
    private fun reSet() {
        for (v in allSelectedViews) {
            v.visibility = View.INVISIBLE
        }
        //清空
        allSelectedViews.clear()
        lastSelectedView = null
        Log.v("cu", "$password")
        password.clear()
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val location:Point = transform(event!!)
        //判断是否在操作区域内
        if(!(location.x>=0&&location.x<=mcontainer.width)&&(location.y>=0&&location.y<=mcontainer.height)){
            return true
        }

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                findViewThatContainsPoint(transform(event)).also {
                    highLightView(it)
                }

            }
            MotionEvent.ACTION_MOVE -> {
                findViewThatContainsPoint(transform(event)).also {
                    highLightView(it)
                }

            }
            MotionEvent.ACTION_UP -> {
                if (orgPassword == null) {
                    //判断是设置密码的第几次
                    if (firstPassword == null) {
                        firstPassword = password.toString()
                        mAlert.text = "请确认密码"
                    } else {
                        //确认密码
                        if (firstPassword == password.toString()) {
                            //两次密码一致
                            mAlert.text = "设置成功"
                            SharedpreferenceUtil.getInstance(this).savePassword(firstPassword!!)
                        } else {
                            mAlert.text = "两次密码不同"
                            firstPassword = null
                        }
                    }
                } else {
                    //确认密码
                    if (firstPassword == password.toString()) {
                        //两次密码一致
                        mAlert.text = "设置成功"
                        SharedpreferenceUtil.getInstance(this).savePassword(firstPassword!!)
                    } else {
                        mAlert.text = "两次密码不同"
                        firstPassword = null
                    }
                    reSet()
                }

            }

        }
        return false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mHeader.setOnClickListener {
            Intent().apply {
                action = Intent.ACTION_PICK
                type = "image/*"
                startActivityForResult(this,RequestImageCode)
            }
        }
        //获取头像
        File(filesDir,"header.jpg").also {
            if (it.exists()){
                BitmapFactory. decodeFile(it.path).also {image->
                    mHeader.setImageBitmap(image)

                }
            }
        }


        //获取密码
        SharedpreferenceUtil.getInstance(this).getPassword().also {
            if (it == null) {
                mAlert.text = "请设置密码"
            } else {
                mAlert.text = "请解锁"
                orgPassword = it
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            RequestImageCode-> {
                if (resultCode != Activity.RESULT_CANCELED) {
                    val uri = data?.data
                    uri?.let {
                        contentResolver.openInputStream(uri).use {
                            BitmapFactory.decodeStream(it).also { image ->
                                mHeader.setImageBitmap(image)
                                //缓存图片
                                val file = File(filesDir,"header.jpg")
                                FileOutputStream(file).also {fos->
                                    image.compress(Bitmap.CompressFormat.JPEG,50,fos)

                                }

                            }
                        }
                    }

                }
            }
            RequestVideoCode->{

            }

    }

    }}


    
