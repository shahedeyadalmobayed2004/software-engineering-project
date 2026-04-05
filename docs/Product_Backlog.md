User Stories & Backlog 

Step 1: Identify User Types 

User Types:

1. Home Chef / Foodie (Primary)
Individuals passionate about cooking who are looking for a single place to save their experiences.
Requirements: Save recipes, browse other people's recipes, quick access to ingredients.
2. Content Creator (Primary)
Chefs who want to share their work with others professionally.
Requirements: Display recipes with photos and videos, easily manage content (CRUD).
3. Casual Browser / Guest (Secondary)
Users looking for quick meal inspiration without having to create an account.
Requirements: Browse the latest recipes, explore categories, watch preparation methods.


Step 2: Write User Stories 
User Stories (Total: 18 Stories)
Must-Have Stories (Priority 1: Sprints 1-2)
Story 1: User Registration
As a new user, I want to create an account using my email and password ,so that I can save my personal recipes in the cloud
Priority: Must-Have 
Est. Points: 5

Story 2: Recipe Creation (CRUD)
As a Content Creator, I want to add a new recipe with title, ingredients, and steps, so that I can document my cooking process.
Priority: Must-Have 
Est. Points: 8

Story 3: Media Upload
As a user, I want to upload photos of my dishes to Cloudinary, so that my recipes look appealing and professional.
Priority: Must-Have 
Est. Points: 5

Story 4: Smart Filtering
As a foodie, I want to filter recipes by category (e.g., Breakfast, Vegan), so that I can find what to cook based on my current need.
Priority: Must-Have 
Est. Points: 3

Story 5: Offline Access
As a user, I want to view my saved recipes even when I don't have internet, so that I can cook anywhere.
Priority: Must-Have 
Est. Points: 5

Story 6: Favorites List
As a browser, I want to mark recipes as "Favorite", so that I can quickly access them later from my profile.
Priority: Must-Have 
 Est. Points: 3

Story 7: Recipe Search
As a user, I want to search for recipes by name, so that I don't have to scroll through the entire list.
Priority: Must-Have 
 Est. Points: 3

Story 8: Secure Login
As a returning user, I want to log in securely, so that I can access my private recipe collection.
Priority: Must-Have 
Est. Points: 2

Should-Have Stories (Priority 2 : Sprints 3-4)
Story 9: Search by Ingredients
As a user, I want to enter the ingredients I have in my fridge, so that the app suggests recipes I can actually make now.
Priority: Should-Have 
Est. Points: 8

Story 10: Social Interaction (Likes)
As a Content Creator, I want to receive "Likes" on my recipes, so that I know people appreciate my work.
Priority: Should-Have 
Est. Points: 3

Story 11: Shopping List Generation
As a foodie, I want to click a button to turn recipe ingredients into a checklist, so that I can use it while grocery shopping.
Priority: Should-Have 
Est. Points: 5

Story 12: Social Comments
As a user, I want to leave comments on recipes, so that I can ask questions or share my results with the creator.
Priority: Should-Have 
 Est. Points: 5

Story 13: Advanced Filter (Calories/Time)
As a health-conscious user, I want to filter recipes by calorie count and prep time, so that I can manage my diet.
Priority: Should-Have 
 Est. Points: 5

Story 14: Profile Management
As a user, I want to update my profile picture and bio, so that I can build my identity on the platform.
Priority: Should-Have 
 Est. Points: 3

Nice-to-Have Stories (Priority 3: Sprints 5-6)
Story 15: PDF Export
As a user, I want to export a recipe as a PDF, so that I can print it or keep a digital copy on my device.
Priority: Nice-to-Have 
 Est. Points: 5

Story 16: Share to WhatsApp
As a user, I want to share a recipe link via WhatsApp, so that I can send it to my friends and family easily.
Priority: Nice-to-Have 
 Est. Points: 3

Story 17: Video Link Support
As a creator, I want to embed a YouTube link in my recipe, so that users can watch the preparation steps.
Priority: Nice-to-Have 
 Est. Points: 3

Story 18: Nutritional Dashboard
As a user, I want to see a summary of nutrients for each recipe, so that I can track my intake.
Priority: Nice-to-Have 
Est. Points: 8

Step 3: Add Acceptance Criteria 

User Story 1: User Registration

As a new user
I want to create an account with my email and password
So that I can save and manage my personal recipes in the cloud
Priority: Must-Have
Estimated Points: 5
Acceptance Criteria:
1. Given I am on the registration page
When I enter valid details (name, email, password 8+ chars)
Then my account should be created in Firebase
And I should be redirected to the Home screen.
2. Given I enter an invalid email format
When I click "Register"
Then I should see an error message "Invalid email format".
3. Given I enter a password with less than 8 characters
When I click "Register"
Then I should see "Password must be at least 8 characters".
4. Given the email is already registered
When I try to sign up
Then I should see "Account already exists, please login".

Technical Notes:
    Use Firebase Authentication.
    Email validation using Android Patterns.
Dependencies: - Firebase Project configured in Android Studio.
Definition of Done:
    [ ] Account created in Firebase Console.
    [ ] UI displays success/error toasts.

User Story 2: Create New Recipe (CRUD)
As a home chef
I want to add a new recipe with ingredients and photos
So that I can document my cooking creations
Priority: Must-Have
Estimated Points: 8
Acceptance Criteria:
 1. Given I am on the "Add Recipe" screen
When I enter the title, ingredients list, and select a category
And click "Save"
Then the recipe should be stored in Firestore.
2. Given I am filling the form
When I leave the "Title" field empty
Then the save button should be disabled or show an error "Title is required".
3. Given I have a recipe image
When I select it from gallery
Then it should be uploaded to Cloudinary and return a valid URL.

Technical Notes:
  Use Firestore add() method.
  Integrate Cloudinary Android SDK for image hosting.
Dependencies:
  Cloudinary API keys configured.
  Firestore rules set to "authenticated users only".
Definition of Done:
    [ ] Recipe appears in the user's list immediately.
    [ ] Image loads correctly via Picasso/Glide.

User Story 3: Search by Ingredients
As a user with limited supplies
I want to search for recipes by entering specific ingredients
So that I can find something to cook with what I have
Priority: Should-Have
Estimated Points: 8
Acceptance Criteria: 1. Given I enter "Potato" in the ingredient search bar
When I click search
Then the app should display all recipes that contain "Potato" in their ingredients list.
2. Given I enter an ingredient that doesn't exist in any recipe
When I search
Then I should see a "No recipes found matching these ingredients" message.

Technical Notes:
  Use Firestore query: whereArrayContains("ingredients", searchInput).
Dependencies:
  Database must have recipes with ingredients stored as an Array.
Definition of Done:
  [ ] Search results are accurate and filtered in real-time.

User Story 4: Offline Mode Access
As a chef in a kitchen with poor Wi-Fi
I want to browse my saved recipes without an internet connection
So that I can follow the instructions while cooking
Priority: Must-Have
Estimated Points: 5
Acceptance Criteria: 1. Given I have previously opened the app with internet
When I turn off Wi-Fi and open the app
Then I should still see my previously loaded recipes.
2. Given I am offline
When I try to add a new recipe
Then the app should save it locally and sync it once internet is restored.

Technical Notes: 
   Enable Firestore Offline Persistence: FirebaseFirestoreSettings.
Dependencies:
   Sufficient local storage on the device.
Definition of Done:
   [ ] App works in "Airplane Mode" for cached data.

User Story 5: Export Recipe to PDF
As a user who likes physical copies
I want to convert my recipe into a PDF file
So that I can print it or share it as a document
Priority: Nice-to-Have
Estimated Points: 5
Acceptance Criteria:
 1. Given I am viewing a recipe's details
When I click the "Export PDF" button
Then a PDF file should be generated with the recipe title, image, and steps.
2. Given the PDF is generated
When the process is finished
Then I should see a notification "PDF saved to downloads".

Technical Notes: 
   Use Android PdfDocument class or iText library.
Dependencies: 
   Storage Write Permissions for Android 10 and below.
Definition of Done: 
   [ ] PDF file opens correctly in any PDF viewer.

User Story 6: Shopping List Checklist
As a shopper
I want to convert recipe ingredients into a digital shopping list
So that I can check off items as I buy them in the store
Priority: Should-Have
Estimated Points: 5
Acceptance Criteria: 1. Given I am on a recipe page
When I click "Add to Shopping List"
Then all ingredients should be added to my personal checklist.
2. Given I am at the store
When I tap on an ingredient in the list
Then it should be marked as "bought" with a strikethrough effect.

Technical Notes: 
   Use RecyclerView with CheckBox and Paint.STRIKE_THRU_TEXT_FLAG.
Dependencies: 
   Local database (Room) or Firestore collection for shopping items.
Definition of Done: 
   [ ] List persists even after closing the app.


User Story 7: Favorites List
As a foodie
I want to mark recipes as "Favorite"
So that I can quickly access them later from my profile without searching
Priority: Must-Have
Estimated Points: 3
Acceptance Criteria:
1.Given I am viewing a recipe's details
When I click on the "Heart" (Favorite) icon
Then the icon should change color (e.g., Red)
And the recipe ID should be added to my "Favorites" collection in Firestore.
2.Given I am in my "Profile" screen
When I click on "My Favorites" tab
Then I should see a list of only the recipes I have marked as favorites.
3.Given a recipe is already in my favorites
When I click the "Heart" icon again
Then the icon should return to its original state
And the recipe should be removed from my favorites list.

Technical Notes:
   Store Favorites as a sub-collection under the User's document in Firestore.
   Use NotifyDataSetChanged() to update the list in real-time.
Dependencies:
   User must be authenticated. 
Definition of Done:
   [ ] Favorite status persists after restarting the app.
   [ ] UI reflects changes instantly.

User Story 8: Social Interaction (Likes)
As a Content Creator
I want to see how many people "Liked" my recipe
So that I can know which of my recipes are popular
Priority: Should-Have
Estimated Points: 3
Acceptance Criteria:
1.Given I am browsing recipes
When I click the "Like" button on a recipe card
Then the like count should increase by 1.
2.Given I have already liked a recipe
When I click the "Like" button again
Then the like count should decrease by 1.
3.Given multiple users are liking the recipe at the same time
When they click "Like"
Then Firestore Transactions should be used to ensure the count is updated accurately without race conditions.

Technical Notes:
   Use FieldValue.increment(1) for the like counter in Firestore.
   Implement a "Likes" collection to track which user liked which recipe (to prevent multiple likes from the same person). 
Dependencies:
   Firestore "Recipes" collection must have a likesCount field.
Definition of Done: 
   [ ] Like count updates accurately across different devices.


User Story 9: Social Comments
As a user
I want to leave a comment on a recipe
So that I can ask the chef a question or share my feedback
Priority: Should-Have
Estimated Points: 5
Acceptance Criteria:
1.Given I am on the recipe details page
When I type a message in the comment box and click "Post"
Then my comment should appear at the bottom of the list with my name and timestamp.
2.Given I am viewing comments
When there are more than 10 comments
Then I should be able to scroll through them using a ScrollView or RecyclerView.
3.Given I posted a comment by mistake
When I long-press my own comment
Then I should see an option to "Delete" my comment.

Technical Notes:
Comments should be stored in a sub-collection called comments under each specific recipe document.
Use Firebase Timestamp to sort comments from oldest to newest
Dependencies:
   Recipe document ID must be passed to the comments activity/fragment. 
Definition of Done:
   [ ] Users can successfully add and view comments.
   [ ] Comments are tied correctly to the specific recipe.


Product Backlog: RecipeBook Platform
1. Backlog Summary
Total User Stories: 18
Must-Have (Priority 1): 8 Stories (Sprints 1-2)
Should-Have (Priority 2): 6 Stories (Sprints 3-4)
Nice-to-Have (Priority 3): 4 Stories (Sprints 5-6)

2. Must-Have Stories (Priority 1)
A. Authentication & User Management
Story 1: User Registration [5 points]
 As a new user,
 I want to create an account with my email and password,
 so that I can save and manage my personal recipes in the cloud.

Acceptance Criteria:
Given I am on the registration page,
When I enter valid details (name, email, password 8+ chars), 
Then my account should be created in Firebase and I should be redirected to the Home screen.

Given I enter an invalid email format,
When I click "Register", 
Then I should see an error "Invalid email format".

Given I enter a password < 8 characters,
When I click "Register",
Then I should see error "Password must be at least 8 characters".

Given the email is already registered,
When I try to sign up,
Then I should see "Account already exists, please login".

Technical Notes:
    Use Firebase Authentication SDK

Definition of Done:
Account created in Firebase Console; UI displays success/error toasts.


Story 8: Secure Login [2 points] 
As a returning user,
I want to log in securely, 
so that I can access my private recipe collection.

Acceptance Criteria: Given I am on the login page,
When I enter my registered email and correct password,
Then I should be granted access to my dashboard.

B. Core Functionality

Story 2: Create New Recipe (CRUD) [8 points]
As a home chef,
I want to add a new recipe with ingredients and photos,
so that I can document my cooking creations.

Acceptance Criteria:

Given I am on the "Add Recipe" screen,
When I fill title, ingredients, and category and click "Save",
Then the recipe should be stored in Firestore.

Given I leave the "Title" field empty,
When I click save,
Then I should see an error "Title is required".

Given I have a recipe image,
When I select it from gallery,
Then it should be uploaded to Cloudinary and return a valid URL.

Technical Notes:
Use Firestore add() method;
Integrate Cloudinary Android SDK.

Definition of Done:
Recipe appears in list;
Image loads correctly via Picasso/Glide.

Story 3: Media Upload [5 points]
 As a user
 I want to upload photos of my dishes to Cloudinary,
 so that my recipes look appealing and professional

Story 4: Smart Filtering [3 points] 
As a foodie,
I want to filter recipes by category,
so that I can find what to cook based on my current need.

Story 5: Offline Access [5 points]
 As a chef in a kitchen with poor Wi-Fi,
 I want to browse my saved recipes without an internet connection.

Acceptance Criteria:
Given I have previously opened the app with internet,
When I turn off Wi-Fi,
Then I should still see my previously loaded recipes.

Given I am offline,
When I add a recipe,
Then the app should save it locally and sync once internet is restored.

Technical Notes:
Enable Firestore Offline Persistence.

Story 6: Favorites List [3 points] 
As a foodie,
I want to mark recipes as "Favorite",
so that I can quickly access them later.

Acceptance Criteria:
Given I am on recipe details,
When I click the "Heart" icon,
Then the icon should change color and the ID be added to "Favorites" in Firestore.

Given I am in my "Profile",
When I click "My Favorites",
Then I should see only my favorited recipes.

Story 7: 
Recipe Search [3 points] 
As a user,
I want to search for recipes by name,
so that I don't have to scroll through the list.

3. Should-Have Stories (Priority 2)

Story 9: Search by Ingredients [8 points]

As a user with limited supplies,
I want to enter ingredients I have,
so that the app suggests recipes I can actually make.

Acceptance Criteria:
Given I enter "Potato",
When I click search,
Then the app should display all recipes containing "Potato".

Story 10: Social Interaction (Likes) [3 points] 
As a Content Creator,
I want to receive "Likes" on my recipes,
So that I know people appreciate my work and to increase the visibility of my recipes.

Acceptance Criteria:
Given multiple users like a recipe at once,
When they click "Like",
Then Firestore Transactions should ensure the count is updated accurately.

Story 11: Shopping List Generation [5 points]
 As a shopper,
 I want to turn recipe ingredients into a checklist,
 So that I can easily track what I need to buy while I am at the grocery store.

Acceptance Criteria:
 Given I am on a recipe page, 
 When I click "Add to Shopping List",
Then ingredients should be added to a checklist with a strikethrough effect when tapped.

Story 12: Social Comments [5 points] 
As a user,
I want to leave comments on recipes,
So that I can ask the chef questions, share my feedback, or tell others how the recipe turned out.

Acceptance Criteria:
Given I posted a comment by mistake,
When I long-press my own comment,
Then I should see an option to "Delete" it.

Story 13: Advanced Filter (Calories/Time) [5 points]
As a health-conscious user,
I want to filter recipes by calorie count and preparation time,
So that I can find meals that fit my diet and my schedule.

Story 14: Profile Management [3 points]
As a user,
I want to update my profile picture and bio,
So that I can build my identity on the platform.

4. Nice-to-Have Stories (Priority 3)

Story 15: PDF Export [5 points] 
As a user who likes physical copies,
I want to convert my recipe into a PDF file.

Acceptance Criteria:
Given I click "Export PDF",
When finished, Then I should see a notification "PDF saved to downloads".

Story 16: Share to WhatsApp [3 points] 
As a user,
I want to share a recipe link via WhatsApp,
So that I can easily send it to my friends and family.

Story 17: Video Link Support [3 points]
 As a creator,
I want to embed a YouTube link in my recipe,
So that users can watch the preparation steps visually.

Story 18: Nutritional Dashboard [8 points]
As a user,
I want to see a summary of nutrients (Carbs, Protein, Fats),
So that I can track my daily intake accurately.

5. Icebox (Future Considerations)
AI Meal Planner: Generate weekly plans based on preferences.
Dark Mode: Support for system-wide dark theme.
Voice Commands: Hands-free "Next Step" voice control while cooking.

