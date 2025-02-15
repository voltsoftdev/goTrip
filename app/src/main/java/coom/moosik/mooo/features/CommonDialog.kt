package coom.moosik.mooo.features

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import coom.moosik.mooo.R
import coom.moosik.mooo.extensions.screenWidth

class CommonDialog(context: Context, vararg messages : String?, buttonCount : ButtonCount) : Dialog(context), View.OnClickListener
{
    var mStrTitle : String? = ""
    var mStrMessage : String? = ""
    var mStrSubMessage : String? = ""
    var mButtonText1 : String? = ""
    var mButtonText2 : String? = ""
    var mCheckBoxText : String? = ""
    var mConfirmed : Boolean = false
    var mChecked : Boolean = true
    var mButtonCount : ButtonCount = ButtonCount.TWO

    lateinit var mScrollView : ScrollView
    lateinit var mTitleView : TextView
    lateinit var mMessageView1 : TextView
    lateinit var mMessageView2 : TextView
    lateinit var mCancelButton : Button
    lateinit var mConfirmButton : Button
    lateinit var checkBoxFrameView : View
    lateinit var checkBox : ToggleButton
    lateinit var checkBoxTextView : TextView

    enum class ButtonCount { ONE , TWO }

    init {
        val paramsLength = messages.size

        mStrTitle = (if (paramsLength > 0) messages[0] else null)
        mStrMessage = (if (paramsLength > 1) messages[1] else null)
        mStrSubMessage = (if (paramsLength > 2) messages[2] else null)
        mButtonText1 = (if (paramsLength > 3 && !TextUtils.isEmpty(messages[3]))
            messages[3] else "확인")
        mButtonText2 = (if (paramsLength > 4 && !TextUtils.isEmpty(messages[4]))
            messages[4] else "취소")
        mCheckBoxText = (if (paramsLength > 5) messages[5] else "")

        mButtonCount = buttonCount
    }

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val layoutParams = window!!.attributes
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.windowAnimations = R.style.CustomDialogAnimation
        layoutParams.dimAmount = 0.5f
        window!!.attributes = layoutParams

        setContentView(R.layout.view_basedialog)

        mScrollView = findViewById(R.id.resizeableFrame)
        mScrollView.layoutParams.width = context.screenWidth()

        mTitleView = findViewById(R.id.dialogTitleView)
        mTitleView.visibility = if (!TextUtils.isEmpty(mStrTitle)) View.VISIBLE else View.GONE
        mTitleView.text = mStrTitle
        mMessageView1 = findViewById(R.id.dialogMessageView1)
        mMessageView1.visibility = if (!TextUtils.isEmpty(mStrMessage)) View.VISIBLE else View.GONE
        mMessageView1.text = mStrMessage
        mMessageView2 = findViewById(R.id.dialogMessageView2)
        mMessageView2.visibility = if (!TextUtils.isEmpty(mStrSubMessage)) View.VISIBLE else View.GONE
        mMessageView2.text = mStrSubMessage

        mCancelButton = findViewById(R.id.cancelButton)
        mCancelButton.setOnClickListener(this)
        mConfirmButton = findViewById(R.id.confirmButton)
        mConfirmButton.setOnClickListener(this)

        checkBoxFrameView = findViewById(R.id.checkBoxFrame)
        checkBoxFrameView.visibility = if (TextUtils.isEmpty(mCheckBoxText)) View.GONE else View.VISIBLE
        checkBox = findViewById(R.id.checkButton)
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            checkBoxTextView.setTextColor(if (isChecked)
                ContextCompat.getColor(context, R.color.color_black) else ContextCompat.getColor(context, R.color.color_black50))
        }
        checkBoxTextView = findViewById(R.id.checkButtonText)
        checkBoxTextView.text = mCheckBoxText
        checkBoxTextView.setOnClickListener {
            checkBox.isChecked = !checkBox.isChecked
        }

        if (mButtonCount == ButtonCount.TWO) {
            mConfirmButton.visibility = View.VISIBLE
            mConfirmButton.text = mButtonText1
            mCancelButton.visibility = View.VISIBLE
            mCancelButton.text = mButtonText2
        }
        else
        {
            mConfirmButton.visibility = View.VISIBLE
            mConfirmButton.text = mButtonText1
            mCancelButton.visibility = View.GONE
        }
    }

    override fun onClick(v: View?)
    {
        mChecked = checkBox.isChecked

        when (v?.id)
        {
            R.id.cancelButton ->
            {
                mConfirmed = false

                dismiss()
            }

            R.id.confirmButton ->
            {
                mConfirmed = true

                dismiss()
            }
        }
    }
}