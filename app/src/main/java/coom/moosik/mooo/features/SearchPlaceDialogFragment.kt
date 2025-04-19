package coom.moosik.mooo.features

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import coom.moosik.mooo.model.Marker

class SearchPlaceDialogFragment(
    private val onPlaceSelected: (Marker) -> Unit
) : DialogFragment() {

    private val sharedViewModel : MapPageViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {

                val searchedMarkers by sharedViewModel.searchedMarkers.collectAsState()

                SearchPlaceDialogContent(
                    modifier = Modifier.fillMaxSize().fillMaxHeight(),
                    onSearch = {
                        sharedViewModel.searchMarkers(it)
                    },
                    searchedMarkers = searchedMarkers ?: emptyList(),
                    onPlaceClick = {
                        onPlaceSelected(it)

                        dismiss() // DialogFragment 닫기
                    })
            }
        }
    }
}

@Composable
fun SearchPlaceDialogContentPreview(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit,
    searchedMarkers : List<Marker>,
    onPlaceClick: (Marker) -> Unit) {
    SearchPlaceDialogContent(modifier = modifier, onSearch, searchedMarkers, onPlaceClick)
}

@Composable
fun SearchPlaceDialogContent(
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit,
    searchedMarkers : List<Marker>,
    onPlaceClick: (Marker) -> Unit) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            val searchText = remember { mutableStateOf("") }

            OutlinedTextField(
                value = searchText.value,
                onValueChange = {
                    searchText.value = it // 텍스트 필드의 상태 업데이트
                    onSearch(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("검색하고자 하는 장소 이름을 입력해주세요") },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(4.dp)
            )

            if (searchedMarkers.isNotEmpty()) {
                LazyColumn {

                    items(searchedMarkers) { marker ->
                        Text(
                            text = marker.irm1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { onPlaceClick(marker) },
                            // 클릭 시 원하는 동작 추가 (예: 해당 마커로 이동)
                        )
                    }
                }
            } else if (searchText.value.isNotEmpty()) {
                Text(text = "검색 결과가 없습니다.", modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}