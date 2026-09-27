package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.isSameDayAsToday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Bd Tournament", appName)
  }

  @Test
  fun `verify isSameDayAsToday matches current date format`() {
    val df = SimpleDateFormat("M/d/yyyy, h:mm:ss a", Locale.US)
    val todayStr = df.format(Date())
    assertTrue(isSameDayAsToday(todayStr))
    assertFalse(isSameDayAsToday("1/1/2020, 10:00:00 AM"))
  }
}
