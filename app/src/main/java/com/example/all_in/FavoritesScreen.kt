package com.example.all_in

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.all_in.api.QuoteResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: QuoteViewModel) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var J_list by remember { mutableStateOf(emptyList<QuoteResponse>()) }
    LaunchedEffect(userId) {
        userId?.let {

           J_list = viewModel.getFavoriteQuotes(it)
        }
    }
    Log.d("FavoritesScreen", "Favorite quotes: $J_list")
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Favorite Quotes") })
        }

    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if(J_list.isEmpty()) {
                item {
                    Text(text = "No favorite quotes yet!", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            } else {
                items(J_list) { quote ->
                    QuoteCardFav(quote, true, {}, {
                        removeFromFavorites(it._id)
                        // Trigger recomposition or refresh the list
                        J_list = J_list.filter { q -> q._id != it._id }
                    })
                }
            }
        }
    }
}

fun removeFromFavorites(id: String) {
    try {
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get userId in the composable

        val db = FirebaseFirestore.getInstance()
        db.collection("favoriteQuotes")
            .whereEqualTo("quoteId", id)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("favoriteQuotes").document(document.id).delete()
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting document", e)
            }

        Log.d("Favorites", "Removing quote from favorites , id: $id , user: $userId")
    } catch (e: Exception) {
        Log.e("Favorites", "Error removing quote from favorites", e)

    }
}

@Composable
fun QuoteCardFav(
    quote: QuoteResponse,
    isFavorite: Boolean,
    onAddToFavorites: (QuoteResponse) -> Unit,
    onRemoveFromFavorites: (QuoteResponse) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "\"${quote.content}\"", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                )
                {


                    Text(text = "- ${quote.author}", fontSize = 14.sp)
                    IconButton(onClick = {
                        onRemoveFromFavorites(quote)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove from favorites"
                        )
                    }
                }
            }
        }
    }
}
