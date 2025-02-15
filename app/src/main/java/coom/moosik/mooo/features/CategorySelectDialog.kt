package coom.moosik.mooo.features

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment

data class Category(
    val id: Int,
    val name: String,
    val subCategories: List<SubCategory> = emptyList()
)

data class SubCategory(
    val id: Int,
    val name: String
)

class CategorySelectDialog : DialogFragment() {

//    private val sharedViewModel : MapPageViewModel by activityViewModels<MapPageViewModel>()

    @Composable
    fun CategoryCard(
        category: Category,
        onCategorySelected: (Category, SubCategory?) -> Unit
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = "카테고리 선택")
                Button(onClick = {

                }) {
                    Text(text = "카테고리 선택")
                }
            }
        }
    }
}