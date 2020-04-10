package com.example.gps.Tools

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsApplications(val context: Context) {

    fun hasPermissions(listPermissions:Array<String>):Boolean{

        var answer:Boolean = true

        for(permission in listPermissions)
        {
            if(ContextCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED)
            {
                answer= false
                break
            }
        }

        return answer
    }


    fun acceptPermission(listPermissions:Array<String>, requestCode:Int){

        ActivityCompat.requestPermissions(context as Activity,listPermissions,requestCode)
    }

}