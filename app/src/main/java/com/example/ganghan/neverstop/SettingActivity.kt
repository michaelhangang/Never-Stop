package com.example.ganghan.neverstop

import android.app.ActivityOptions
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*

class SettingActivity : AppCompatActivity() {
    lateinit var  switch: Switch
    lateinit var spinner: Spinner
    lateinit var  lengthtextView:TextView
    var length:Long? = null
    var isTimerOn = false
    var handEffect:Int = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        switch = findViewById(R.id.timerSwitch)
        spinner = findViewById(R.id.spinner)
        spinner.isEnabled = false
        lengthtextView = findViewById(R.id.textView5)

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isTimerOn = true
                MyView.isUseTimer=true
                lengthtextView.visibility =View.VISIBLE
                spinner.isEnabled = true
            }else{
                isTimerOn =false
                    MyView.isUseTimer=false
                lengthtextView.visibility =View.INVISIBLE
                spinner.isEnabled = false
            }
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.length_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        this.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                 length= p0?.getItemAtPosition(p2).toString().toLong()
//                MyView.min = length as Long
                when(p0?.getItemAtPosition(p2).toString()){
                    "1 Min" ->  length=1
                    "2 Mins" ->length=2
                    "3 Mins" ->length=3
                    "4 Mins" ->length=4
                    "5 Mins" ->length=5
                    "6 Mins" ->length=6
                    "7 Mins" ->length=7
                    "8 Mins" ->length=8

                }
                MyView.min = length as Long
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                var toast = Toast.makeText(getApplicationContext(), "Please Select One!", Toast.LENGTH_LONG)
                toast.setMargin(50F,50F);
                toast.show()
            }
        }
    }
    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_pirates ->
                    if (checked) {
                        handEffect=2
                    }
                R.id.radio_ninjas ->
                    if (checked) {
                        handEffect=1
                    }
            }
        }
    }
    fun onStartBtClick(view: View) {
        if(isTimerOn&&length==null){
            var toast = Toast.makeText(getApplicationContext(), "Please Select Time Length!", Toast.LENGTH_LONG)
            toast.setMargin(50F,50F);
            toast.show()
            return
        }
        MyView.handEffect=handEffect
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }
}


