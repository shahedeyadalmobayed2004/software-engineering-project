Sprint 1 Plan: 
Foundation, Offline Access & Discovery

Sprint Information
Sprint Number: 1 
Duration: 2 weeks 
Start Date: [04\03\2026]
 End Date: [18\03\2026]
 Team Capacity: 20 hours (18 story points)

Sprint Goal
"Build a seamless user experience by enabling offline access, personalizing content with Favorites, and delivering high-accuracy recipe discovery through smart ingredient-based search and advanced filtering."

Selected User Stories
Story #5: Offline Mode & Persistence
Points: 5 
Assigned to: Shahed Alkhateeb (Lead) 
Priority: Must-Have
 Description: Enable Firestore local data persistence and Picasso caching to allow users to browse recipes without an internet connection. 

Tasks:
[ ] Configure FirebaseFirestoreSettings for offline persistence - Shahed Alkhateeb
[ ] Configure Picasso/Glide disk caching strategy for images - Shahed Alkhateeb
[ ] Add visual indicators for Online/Offline status - Rahaf Elzebda 
Acceptance Criteria:
[ ] Recipes are accessible in Airplane mode.
[ ] Images load from cache when offline.

Story #6/7: Favorites List System
Points: 3
 Assigned to: Shahed Almobayed (Lead)
 Priority: High
 Description: Implement a system to save and access favorite recipes in a dedicated section for quick access.

 Tasks:
[ ] Set up favorites sub-collection structure in Firestore - Shahed Alkhateeb
[ ] Implement toggle logic for adding/removing favorites - Shahed Almobayed
[ ] Design Favorite recipes screen and "Heart" icon toggle - Rahaf Elzebda Acceptance Criteria:
[ ] User can add/remove recipes from favorites.
[ ] Favorites are saved per user profile in Firestore.

Story #9: Search by Ingredients
Points: 8 
Assigned to: Shahed Alkhateeb (Lead)
 Priority: Must-Have 
Description: Develop a smart search engine that filters and displays recipes based on the ingredients available to the user. 

Tasks:
[ ] Implement Firestore whereArrayContainsAny for ingredient matching - Shahed Alkhateeb
[ ] Build search logic to handle multiple ingredient inputs - Shahed Alkhateeb
[ ] Design search bar with dynamic ingredient input (Chips/Tags) - Rahaf Elzebda Acceptance Criteria:
[ ] Search results accurately match selected ingredients.

Story #13: Advanced Smart Filtering
Points: 5
 Assigned to: Rahaf Elzebda (Lead) 
Priority: High
 Description: Enhance the filtering system to include specific criteria such as preparation time, calorie count, and meal type.

 Tasks:
[ ] Update Recipe Model with calories and prepTime fields - Shahed Alkhateeb
[ ] Create Advanced Filter Bottom Sheet/Dialog - Rahaf Elzebda
[ ] Validate filter results for calories and time ranges - Sara Dwima
 Acceptance Criteria:
[ ] Filter results update correctly based on user criteria.

Definition of Done
[ ] All tasks completed.
[ ] Code follows team standards (Android/Kotlin/Java).
[ ] All acceptance criteria met.
[ ] Code reviewed by at least one team member.
[ ] Merged to main branch.
[ ] Manually tested by Product Owner.

Sprint Schedule (Week 3-4)
Week 1
Monday: Sprint planning meeting, Setup Firestore persistence.
Wednesday: Daily standup, Start Search by Ingredients logic.
Friday: Complete Favorites list backend, Progress on Search UI.
Week 2
Monday: Complete Search by Ingredients logic and start Advanced Filtering UI.
Wednesday: Daily standup, Integration of filtering with search.
Friday: Final testing (Offline mode & Search accuracy), Sprint review.

Success Metrics
[ ] Sprint goal achieved.
[ ] All selected stories completed (21 points).
[ ] Working offline mode and smart search demonstrated.

Prepared by: Shahed Eyad Al Mobayed (Scrum Master) 
Date: 18\02\2026

Reviewed by Team:
 All team member names

Team Commitment:
We, the Commit & Chill team, commit to delivering these features through continuous collaboration and technical excellence to ensure the success of the RecipeBook project.

Signatures: Shahed Almobayed, Shahed Alkhateeb, Rahaf Elzebda, Sara Dwima
