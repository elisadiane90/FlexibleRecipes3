package com.example.flexiblerecipes.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.flexiblerecipes.MainActivity
import com.example.flexiblerecipes.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.net.MalformedURLException
import java.net.URL

class HomeFragment : Fragment() {

    private lateinit var editText: EditText
    private lateinit var fetchButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        editText = root.findViewById(R.id.urltxt)
        fetchButton = root.findViewById(R.id.fetch)

        fetchButton.setOnClickListener {
            val url = editText.text.toString()
            fetchRecipeData(url)
        }

        return root
    }

    private fun fetchRecipeData(url: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val urlObject = URL(url)
                val doc = Jsoup.parse(urlObject, 3000)
                val recipeData = extractRecipeData(doc)
                requireActivity().runOnUiThread {
                    (requireActivity() as MainActivity).displayRecipeData(recipeData)
                }
            } catch (e: MalformedURLException) {
                requireActivity().runOnUiThread {
                    // Handle invalid URL
                    // Show error message to the user
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    // Handle other exceptions
                    // Show error message to the user
                }
            }
        }
    }

    private fun extractRecipeData(doc: org.jsoup.nodes.Document): MainActivity.RecipeData {
        // Extract recipe details using CSS selectors
        val prepTime = doc.select("#mntl-recipe-card__details_1-0 > div > div:nth-child(1) > div.mntl-recipe-details__label").text()
        val cookTime = doc.select("#mntl-recipe-card__details_1-0 > div > div:nth-child(2) > div.mntl-recipe-details__label").text()
        val additionalTime = doc.select("#mntl-recipe-card__details_1-0 > div > div:nth-child(3) > div.mntl-recipe-details__label").text()
        val totalTime = doc.select("#mntl-recipe-card__details_1-0 > div > div:nth-child(4) > div.mntl-recipe-details__label").text()
        val servings = doc.select("#mntl-recipe-card__details_1-0 > div > div:nth-child(5) > div.mntl-recipe-details__label").text()

        // Extract ingredients using CSS selector
        val ingredients = doc.select("#mntl-structured-ingredients_1-0 > ul li").map { it.text() }

        // Extract directions using CSS selector
        val directions = doc.select("#recipe__steps-content_1-0 ol, #recipe__steps-content_1-0 ul")
            .flatMap { it.select("li").map { li -> li.text() } }

        return MainActivity.RecipeData(
            prepTime,
            cookTime,
            additionalTime,
            totalTime,
            servings,
            ingredients,
            directions
        )

    }
}
