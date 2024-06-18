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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.example.all_in.api.QuotableApi
import com.example.all_in.api.QuoteResponse
import com.example.all_in.api.RetrofitInstance // Assuming you have this class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException

class QuoteViewModel(private val quotableApi: QuotableApi = RetrofitInstance.quotableApi) : ViewModel() {

    val Q_NB = 10
    private val _quoteState = MutableStateFlow<QuoteState>(QuoteState.Loading)
    val quoteState: StateFlow<QuoteState> = _quoteState.asStateFlow()

    init {
        getMultipleRandomQuotes(Q_NB)
    }

    fun getMultipleRandomQuotes(count: Int) {
        _quoteState.value = QuoteState.Loading

        viewModelScope.launch {
            try {
                val quotes = List(count) { quotableApi.getRandomQuote() } // Fetch multiple quotes
                _quoteState.value = QuoteState.Success(quotes)
            } catch (e: IOException) {
                _quoteState.value = QuoteState.Error("Network error")
            } catch (e: Exception) {
                _quoteState.value = QuoteState.Error("An unexpected error occurred")
            }
        }
    }

    private val _favoriteQuotes = MutableStateFlow<List<QuoteResponse>>(emptyList())
    val favoriteQuotes: StateFlow<List<QuoteResponse>> = _favoriteQuotes.asStateFlow()

    suspend fun getFavoriteQuotes(userId: String): List<QuoteResponse> {
        val favoriteQuoteIds = mutableListOf<String>()

        try {
            val firestore = FirebaseFirestore.getInstance()

            val result = firestore.collection("favoriteQuotes")
                .whereEqualTo("userId", userId)
                .get()
                .await() // Wait for the result
            favoriteQuoteIds.addAll(
                result.documents.mapNotNull { it.getString("quoteId") } // Extract quoteIds
            )
            Log.d("Favorites", "Got favorite quotes: ${result.documents.mapNotNull { it.getString("quoteId") }}")

        } catch (e: Exception) {
            Log.e("Favorites", "Error getting favorite quotes", e)
        }

        return favoriteQuoteIds.mapNotNull { quoteId ->
            try {
                getQuoteById(quoteId) // Fetch QuoteResponse from API
            } catch (e: Exception) {
                Log.e("Favorites", "Error getting quote by ID", e)
                null // Return null if there's an error
            }
        }


    }


    private suspend fun getQuoteById(id: String): QuoteResponse? {
        return try {
            quotableApi.getQuoteById(id)

        } catch (e: Exception) {
            Log.e("Favorites", "Error getting quote by ID", e)
            null // Return null if there's an error
        }
    }



}

sealed class QuoteState {
    object Loading : QuoteState()
    data class Success(val quotes: List<QuoteResponse>) : QuoteState() // List of quotes
    data class Error(val message: String) : QuoteState()
}

@Composable
fun QuoteScreen(
    firBaseViewModel: ViewModel,
    viewModel: QuoteViewModel,
    onLogout: () -> Unit,
    onNavigateToFavorites: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    var userData by remember(userId) { mutableStateOf<UserData?>(null) }

    fun addToFavorites(id: String)  {
        try {
            val db = FirebaseFirestore.getInstance() // Get Firestore instance
            val data = hashMapOf(
                "quoteId" to id,
                "userId" to userId
            )

            db.collection("favoriteQuotes") // Choose a collection name
                .add(data)
                .addOnSuccessListener { documentReference ->
                    Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error adding document", e)
                }

            Log.d("Favorites", "Adding quote to favorites , id: $id , user: $userId")
        } catch (e: Exception) {
            Log.e("Favorites", "Error adding quote to favorites", e)
            // Handle error in the UI (e.g., Snackbar)
        }
    }

     fun removeFromFavorites(id: String) {
        try {
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

    LaunchedEffect(Unit) {
        val favoriteQuoteIds = viewModel.getFavoriteQuotes(userId!!)
        // Now you have the list of favorite quote IDs, use it as needed
        Log.d("Favorites", "Favorite quote IDs: $favoriteQuoteIds")
    }

    LaunchedEffect(userId) {
        userId?.let {
            val userRef = firestore.collection("users").document(it)
            try {
                val userSnapshot = userRef.get().await() // Use await to get the snapshot
                if (userSnapshot.exists()) {
                    userData = userSnapshot.toObject<UserData>()
                } else {
                    // User document doesn't exist, create it
                    userRef.set(UserData(favoriteQuotes = emptyList())).await()
                }
            } catch (e: Exception) {
                // Handle potential exceptions (e.g., no internet connection)
            }
        }
    }

    val quoteState by viewModel.quoteState.collectAsState()
    val navController = rememberNavController()

    Scaffold(

        floatingActionButton = {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.End)
            {
                FloatingActionButton(onClick = { viewModel.getMultipleRandomQuotes(10) }) { // Refresh button
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
                Spacer(modifier = Modifier.width(16.dp))
                FloatingActionButton(onClick = onNavigateToFavorites) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Favorites")
                }
                Spacer(modifier = Modifier.width(16.dp))
                FloatingActionButton(onClick = {
                    auth.signOut()
                    onLogout()
                }) {
                    Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),

        ) {
            when (quoteState) {
                is QuoteState.Loading -> {
                    item { CircularProgressIndicator(modifier = Modifier.fillMaxWidth()) }
                }

                is QuoteState.Success -> {
                    items((quoteState as QuoteState.Success).quotes) { quote ->
                        QuoteCard(quote, userData?.favoriteQuotes ?: emptyList(),
                            onAddToFavorites = { addToFavorites(it._id) }
                        ) // !! assumes user is logged in
                        {

                            removeFromFavorites(it._id)

                        }

                    }


                }

                is QuoteState.Error -> {
                    item { Text("Error: ${(quoteState as QuoteState.Error).message}") }
                }
            }

        }
    }


}

@Composable
fun QuoteCard(
    quote: QuoteResponse,
    favoriteQuotes: List<String>, // List of favorite quote IDs
    onAddToFavorites: (QuoteResponse) -> Unit,
    onRemoveFromFavorites: (QuoteResponse) -> Unit
) {
    var isFavorite by remember { mutableStateOf(quote._id in favoriteQuotes) }

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
                Row {
                    Text(text = "- ${quote.author}", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (isFavorite) {
                            onRemoveFromFavorites(quote)
                        } else {
                            onAddToFavorites(quote)
                        }
                        isFavorite = !isFavorite
                    }) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites"
                        )
                    }
                }

            }

        }
    }
}
