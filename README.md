This is a straightforward mobile application based on Android, built using the Android Studio framework. The core premise of this application is a simple, user-friendly interface where the user can enter in their monthly expenses and income. The app will then automatically calculate the available budget remaining from subtracting the user's monthly income from the monthly expenses. This is quite simple, so the app also allows the user to enter a calendar view, where they can then tap on individual dates to add a CalendarEvent, which can either be an ExpenseEvent, an IncomeEvent, or a DeadlineEvent. Depending on the type of event it is, the application will either subtract or add from the user's total budget accordingly, which will then be reflected on the app's main page. When the user sets a deadline, notifications will also notify the user in the days leading up to the deadline. The amount of days leading up to the deadline where the user is notified is a user-set preference. 

I encountered a lot of challenges during the process of making this application. It might seem simple, but it really did take a lot of time to make, especially since I was so unfamiliar with Java and Android Studio before this. Some notable challenges off the top of my head were the implementation of core features for the app. For example, in the first iterations of my application, the user was limited to only adding one singular event on each day. It did not support multiple events on a single day, due to the data structure I used to store the CalendarEvents. So I had to think of a new way to represent multiple events on a single day, which meant revamping my old data structures that I previously used. This type of problem solving really forced me to think about the different situations and cases that were possible in my app. My old solution involved a simple hashmap with the integer month as a key, and the CalendarEvent as a value. This worked fine, but I wanted my app to be more precise and robust. I wanted an easy way to keep track of how many CalendarEvents were on a particular date. Even though my CalendarEvents class had data members for the date, this meant I would have to loop through the hashmap everytime, to find out the distribution of CalendarEvents throughout a month. This would be very inefficient.

The two solutions I came up with, then, was to either implement a separate hashmap, with date as the key, and a CalendarEvent as the value. The other, was a hashmap that used the integer month as a key, but instead of simply having a CalendarEvent as the value, I would have a second hashmap as the value. This second hashmap would have a LocalDate for its key, and an ArrayList<CalendarEvent> for it's value. I decided to go with this approach because hashmaps are very effective at grabbing keyed information, and this approach made it more elegant by only using one complex data structure. With this data structure, I would have a way of obtaining all the current events within a month, and also easily view how many CalendarEvents were on a specific day, if there was more than one.

In addition to the challenge discussed above, a local database system also had to be implemented for the application, to preserve user data when the app is closed and reopened. This was challenging in regards to system design, because it is not as simple as storing data and retrieving it. I had to think about how I could break down objects such as CalendarEvents, ExpensesEvents, and IncomeEvents, for example, into data that could easily be stored into the local database. Then, I also had to think about how I would retrieve that data and "reconstruct" the user's data properly, which was also more challenging that expected. 

All in all, this app was simply a hobby project for me. I had just gotten a credit card and income, and was thinking about the future, where I would be more independent and probably have to manage my finances by myself for the most part. So that's how I got the idea for this app; I just wanted something to do for fun while being something that could potentially help me in the future. It ended up being quite fun, and I did learn so much about Java and some advanced concepts which I previously had no idea about, such as interfaces and callbacks. I also got to practice more with databases and systems design, which I didn't have much experience in before this project.