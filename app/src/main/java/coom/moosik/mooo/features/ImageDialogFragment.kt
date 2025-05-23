package coom.moosik.mooo.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.DialogFragment

class ImageDialogFragment(private val imageResId: Int) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ImageViewScreen(imageResId = imageResId)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent) // 투명한 배경 설정
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT) // 필요에 따라 크기 조정
            attributes?.dimAmount = 0.6f // dim 정도 설정 (0.0f ~ 1.0f)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // dim 활성화
        }
    }
}

@Composable
fun ImageViewScreen(imageResId: Int) {
    Image(
        painter = painterResource(id = imageResId),
        contentDescription = null, // 필요에 따라 이미지 설명을 추가하세요.
        modifier = Modifier.fillMaxSize()
    )
}