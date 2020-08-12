package com.example.day9

import android.content.Context
import android.provider.Contacts.SettingsColumns.KEY
import java.security.Key

/**
 *Description
 *by cu
 */
class SharedpreferenceUtil private constructor(){
    private val name = "password"
    companion object{
        private var instance:SharedpreferenceUtil?= null
        private var mContext:Context? = null
        fun getInstance(context: Context):SharedpreferenceUtil{
            mContext = context
            if (instance == null){
                synchronized(this){
                    instance = SharedpreferenceUtil()
                }
            }
            return instance!!
        }
    }

    fun savePassword(pwd:String){
        //获取preference对象
        val sharedPreferences = mContext?.getSharedPreferences(name, Context.MODE_PRIVATE)
        //获取edit对象->写数据
        val edit = sharedPreferences?.edit()
        //写入数据
        edit?.putString(KEY,pwd)
        edit?.apply()
    }

    fun getPassword(): String? {
        val sharedPreferences = mContext?.getSharedPreferences(name, Context.MODE_PRIVATE)
        return sharedPreferences?.getString(KEY,null)
    }
}