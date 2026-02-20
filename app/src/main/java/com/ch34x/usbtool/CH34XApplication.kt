package com.ch34x.usbtool

import android.app.Application
import com.ch34x.usbtool.driver.CH34XDriver

/**
 * 应用程序类，用于全局状态管理
 */
class CH34XApplication : Application() {
    
    companion object {
        private lateinit var instance: CH34XApplication
        
        fun getInstance(): CH34XApplication = instance
    }
    
    val driver: CH34XDriver by lazy {
        CH34XDriver()
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    override fun onTerminate() {
        super.onTerminate()
        driver.disconnect()
    }
}