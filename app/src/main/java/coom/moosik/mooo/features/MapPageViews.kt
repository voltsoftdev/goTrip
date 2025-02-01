package coom.moosik.mooo.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coom.moosik.mooo.composable.notoSansFonts
import coom.moosik.mooo.R

@Preview
@Composable
fun OpenLicenseReportPageTitleBar(modifier: Modifier = Modifier) = ConstraintLayout(modifier = modifier
    .fillMaxWidth()
    .height(56.dp)) {

    val titleLineRef = createRef()
    val subTitleRef = createRef()

    Row(modifier = Modifier
        .constrainAs(titleLineRef) {
            absoluteLeft.linkTo(parent.absoluteLeft)
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
        }
        .fillMaxWidth()
        .wrapContentHeight()
        .background(Color.White)
        .padding(start = 15.dp)) {

        Text(text = stringResource(id = R.string.app_name),
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .align(Alignment.CenterVertically),
            fontSize = 16.sp,
            fontFamily = notoSansFonts,
            color = colorResource(id = R.color.main)
        )

        Spacer(modifier = Modifier
            .width(7.5.dp)
            .fillMaxHeight())

//        Image(
//            painter = painterResource(id = R.drawable.drawable_401b6c),
//            contentDescription = "",
//            modifier = Modifier
//                .width(24.dp)
//                .height(24.dp)
//                .align(Alignment.CenterVertically))
    }

    Text(text = stringResource(id = R.string.app_name),
        modifier = Modifier
            .constrainAs(subTitleRef) {
                absoluteLeft.linkTo(titleLineRef.absoluteLeft)
                bottom.linkTo(titleLineRef.top)
            }
            .wrapContentWidth()
            .wrapContentHeight(),
        fontSize = 12.sp,
        fontFamily = notoSansFonts,
        color = colorResource(id = R.color.main_alpha70))

    val dividerRef = createRef()

    Divider(color = Color.Black, modifier = Modifier
        .constrainAs(dividerRef) {
            bottom.linkTo(parent.bottom)
            absoluteLeft.linkTo(parent.absoluteLeft)
        }
        .fillMaxWidth()
        .height(0.5.dp)
        .padding(start = 15.dp, end = 15.dp))
}