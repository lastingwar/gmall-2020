package com.lastingwar.utils

import java.io.InputStreamReader
import java.util.Properties

/**
 * @author yhm
 * @create 2020-11-05 10:42
 */
object PropertiesUtil {

  def load(propertieName:String): Properties ={
    val prop=new Properties()
    prop.load(new InputStreamReader(Thread.currentThread().getContextClassLoader.getResourceAsStream(propertieName) , "UTF-8"))
    prop
  }
}

