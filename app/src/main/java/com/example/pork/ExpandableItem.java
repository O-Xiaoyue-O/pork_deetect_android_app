package com.example.pork;

public class ExpandableItem {
    private String title;
    private String content;
    private String imageUrl;
    private String recipeTitle;
    private String ingredients;
    private String recipeContent;
    private String date;
//    private String average;

    public ExpandableItem(String title, String content, String imageUrl, String recipeTitle, String ingredients, String recipeContent, String date) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.recipeTitle = recipeTitle;
        this.ingredients = ingredients;
        this.recipeContent = recipeContent;
        this.date = date;
//        this.average = average;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRecipeTitle() {
        return recipeTitle;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getRecipeContent() {
        return recipeContent;
    }

    public String getDate() {
        return date;
    }

//    public String getAverage() {
////        return average;
//    }
}
