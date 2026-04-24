Sprint 1 Plan: Foundation, Automation & Discovery
Sprint Information

Sprint Number: 1

Duration: 2 weeks

Start Date: [04\03\2026]

End Date: [18\03\2026]

Team Capacity: 20 hours (21 story points)

Sprint Goal
"Establish a professional development workflow by implementing Android CI/CD, while enabling offline access and high-accuracy recipe discovery through smart ingredient-based search."

Selected User Stories
Story #1: DevOps & CI/CD Automation

Points: 3

Assigned to: Shahed Almobayed (Lead)

Priority: Must-Have

Description: Setup GitHub Actions Pipeline to automate Linting, Testing, and APK Building for quality assurance.

Tasks:

[ ] Configure GitHub Actions YAML for JDK 17 & Gradle cache - Shahed Almobayed

[ ] Implement automated Linting & Unit Testing steps - Shahed Almobayed

[ ] Automate Debug APK generation and artifact uploading - Shahed Almobayed

Acceptance Criteria:

[ ] Pipeline runs on every Push/Pull Request.

[ ] Reports are generated and APK is downloadable from GitHub.

Story #5: Offline Mode & Persistence

Points: 5

Assigned to: Shahed Alkhateeb (Lead)

Priority: Must-Have

Tasks:

[ ] Configure FirebaseFirestoreSettings for offline persistence - Shahed Alkhateeb

[ ] Configure Picasso/Glide disk caching strategy for images - Shahed Alkhateeb

[ ] Add visual indicators for Online/Offline status - Rahaf Elzebda

Acceptance Criteria:

[ ] Recipes and images are accessible in Airplane mode.

Story #9: Search by Ingredients

Points: 8

Assigned to: Shahed Alkhateeb (Lead)

Priority: Must-Have

Tasks:

[ ] Implement Firestore whereArrayContainsAny logic - Shahed Alkhateeb

[ ] Build search logic for multiple ingredient inputs - Shahed Alkhateeb

[ ] Design search bar with dynamic ingredient input (Chips) - Rahaf Elzebda

Story #13: Advanced Smart Filtering

Points: 5

Assigned to: Rahaf Elzebda (Lead)

Priority: High

Tasks:

[ ] Update Recipe Model (calories/prepTime) - Shahed Alkhateeb

[ ] Create Advanced Filter Bottom Sheet - Rahaf Elzebda

Definition of Done

[ ] All tasks completed & Acceptance criteria met.

[ ] Code follows team standards & Reviewed by a member.

[ ] CI/CD Pipeline passes successfully.

Sprint Schedule 
Week 1

Monday: Sprint planning meeting, Setup Android CI/CD Pipeline (GitHub Actions).

Wednesday: Daily standup, Configure Firestore persistence & Image caching.

Friday: Start Search by Ingredients logic, Initial build check via CI.

Week 2

Monday: Complete Search logic & start Advanced Filtering UI.

Wednesday: Daily standup, UI integration for Search (Chips/Tags).

Friday: Final testing (Offline mode & Pipeline automation), Sprint review.

Success Metrics

[ ] Sprint goal achieved.

[ ] CI/CD Pipeline automatically builds and tests the app.

[ ] Working offline mode and smart search demonstrated.

[ ] All stories completed (21 points).

Prepared by: Shahed Eyad Al Mobayed 
Date: 18\02\2026

Reviewed by Team:
 All team member names

Team Commitment:
We, the Commit & Chill team, commit to delivering these features through continuous collaboration and technical excellence to ensure the success of the RecipeBook project.

Signatures: Shahed Almobayed, Shahed Alkhateeb, Rahaf Elzebda, Sara Dwima
