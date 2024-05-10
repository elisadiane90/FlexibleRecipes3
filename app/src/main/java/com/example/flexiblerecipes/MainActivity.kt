package com.example.flexiblerecipes

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var textView: TextView
    private lateinit var fetchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)

        val result = findViewById<TextView>(R.id.result)
        val fetch = findViewById<Button>(R.id.fetch)

        editText = findViewById(R.id.urltxt)
        textView = findViewById(R.id.textview)
        fetchButton = findViewById(R.id.fetch)

        fetchButton.setOnClickListener {
            var url = editText.text.toString().trim()
            if (url.isNotEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                try {
                    val urlObject = URL(url)
                    scrapeRecipeFromWeb(url)
                } catch (e: MalformedURLException) {
                    textView.text = "Error: Malformed URL: ${e.message}"
                    Log.e("MainActivity", "Malformed URL: ${e.message}")
                }
            } else {
                textView.text = "Please enter a valid URL"
            }
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                fetchButton.performClick()
                true
            } else {
                false
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun scrapeRecipeFromWeb(url: String) {
        Thread {
            val builder = StringBuilder()
            try {
                val doc: Document = Jsoup.connect(url).get()
                val recipeData = extractRecipeData(doc)
                runOnUiThread {
                    displayRecipeData(recipeData)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    textView.text = "Error: ${e.message}"
                    Log.e("MainActivity", "Error occurred: ${e.message}")
                }
            }
        }.start()
    }

    private fun extractRecipeData(doc: Document): RecipeData {
        // Extract recipe details using CSS selectors
        val prepTime = doc.select("#mntl-recipe-card__details_1-0 > div > div:nth-child(1) > div.mntl-recipe-details__value").text()
        val cookTime = doc.select("#mntl-recipe-card__details_1-0 > div > div:nth-child(2) > div.mntl-recipe-details__value").text()
        val additionalTime = doc.select("#mntl-recipe-card__details_1-0 > div > div:nth-child(3) > div.mntl-recipe-details__value").text()
        val totalTime = doc.select("#mntl-recipe-card__details_1-0 > div > div:nth-child(4) > div.mntl-recipe-details__value").text()
        val servings = doc.select("#mntl-recipe-card__details_1-0 > div > div:nth-child(5) > div.mntl-recipe-details__value").text()

        // Extract ingredients using CSS selector
        val ingredients = doc.select("#mntl-structured-ingredients_1-0 > ul li").map { it.text() }

        // Extract directions using CSS selector
        val directions = doc.select("#recipe__steps-content_1-0 ol, #recipe__steps-content_1-0 ul")
            .flatMap { it.select("li").map { li -> li.text() } }

        return RecipeData(
            prepTime,
            cookTime,
            additionalTime,
            totalTime,
            servings,
            ingredients,
            directions
        )
    }


    internal fun displayRecipeData(recipeData: RecipeData) {
        textView.text = "Ingredients:\n${recipeData.ingredients.joinToString("\n")}\n\n" +
                "Directions:\n${recipeData.directions.joinToString("\n")}"
    }

    data class RecipeData(
        val prepTime: String,
        val cookTime: String,
        val additionalTime: String,
        val totalTime: String,
        val servings: String,
        val ingredients: List<String>,
        val directions: List<String>
    )
}
