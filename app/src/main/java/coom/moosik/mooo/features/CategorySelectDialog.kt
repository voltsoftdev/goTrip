package coom.moosik.mooo.features

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coom.moosik.mooo.R
import coom.moosik.mooo.composable.notoSansFonts
import coom.moosik.mooo.model.Category
import coom.moosik.mooo.model.SubCategory
import kotlinx.coroutines.launch

class CategorySelectDialog : DialogFragment() {

    private val sharedViewModel : MapPageViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                DialogLayout(modifier = Modifier.fillMaxSize().fillMaxHeight())
            }
        }
    }

    @Preview
    @Composable
    fun DialogLayout(modifier: Modifier = Modifier) {

        Card(modifier = modifier.background(Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = 5.dp,) {

            Column(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()) {

                TextHeader()

                val list = sharedViewModel.categories.collectAsState()

                CategoryListView(modifier = Modifier
                    .fillMaxSize()
                    .fillMaxHeight(),
                    categories = list.value)
            }
        }
    }

    @Preview
    @Composable
    fun TextHeader(modifier: Modifier = Modifier, text: String = "카테고리 선택") = ConstraintLayout(modifier = modifier
        .fillMaxWidth()
        .wrapContentHeight()) {

        Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)) {

            Text(text = text,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(start = 15.dp, top = 30.dp, bottom = 15.dp)
                    .align(Alignment.CenterStart),
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                fontFamily = notoSansFonts,
                color = colorResource(id = R.color.main_alpha70)
            )
        }
    }

    @Composable
    fun CategoryListView(modifier: Modifier = Modifier, categories : List<SubCategory> = arrayListOf()) {

        LazyColumn(modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()) {

            items(categories) {
                SubCategoryItemView(it)
            }
        }
    }

    @Preview
    @Composable
    fun SubCategoryItemView(subCategory: SubCategory = SubCategory(id = "명승지", name = "명승지")) {

        Box(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White)) {

            Column(modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.TopStart)
                .padding(start = 15.dp, top = 15.dp)) {

                Row {

                    CustomCheckbox(modifier = Modifier.width(24.dp).height(24.dp),
                        subCategory = subCategory)

                    Spacer(modifier = Modifier.width(12.5.dp))

                    val iconIdentifier = LocalContext.current.resources.getIdentifier(
                        subCategory.id, "drawable", LocalContext.current.packageName)
                    Image(
                        painter = painterResource(id = iconIdentifier),
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                        contentDescription = "",
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(15.dp))

                    Text(text = subCategory.name,
                        modifier = Modifier
                            .wrapContentWidth()
                            .wrapContentHeight(),
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Normal,
                        fontFamily = notoSansFonts,
                        color = colorResource(id = R.color.main)
                    )
                }

                Row {
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
        }
    }

    @Composable
    fun CustomCheckbox(
        modifier: Modifier,
        subCategory: SubCategory
    ) {
        var checked by rememberSaveable { mutableStateOf(subCategory.isChecked) }

        IconButton(modifier = modifier, onClick = {
            checked = !checked

            sharedViewModel.toggleCategory(subCategory.id)
        }) {

            Image(
                painter = painterResource(id = R.drawable.check_off),
                contentDescription = "Unchecked"
            )
            AnimatedVisibility(
                modifier = modifier,
                visible = checked,
                exit = shrinkOut(shrinkTowards = Alignment.TopStart) + fadeOut()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.check_on),
                    contentDescription = "Checked"
                )
            }
        }
    }
}
