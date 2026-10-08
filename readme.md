# Hackathon 26s2: attachments

Unlike almost every other modern social media platform, users are not yet able to add an attachment to their posts. This changes today! In this hackathon, we will add support for three different types of attachments: images, polls, and quizzes.

Throughout this hackathon, your team will:
- Add functionality for storing images and allowing users to respond to (multiple-choice) polls and quizzes.
- Persist image data and poll/quiz details and responses between multiple runs of the program.
- Achieve high coverage with unit tests for the code implementing this functionality.
- Ensure a high level of code quality throughout the newly-written code.

The code provided is a version of the miniproject code from a couple weeks ago with basic solutions. An `attachments` module has been created, with (unimplemented) files for `Image`, `Poll`, `Quiz`, `ChoiceAttachmentFactory`, `QuizMarker`. The file `ChoiceAttachmentTests` has been added within the `test` module. Finally, `Post` has a new method called `setAttachment`. There are also two new files unrelated to the code (`uml.png` and `solid-questions.txt`) which are used as part of particular tasks.

You may write your code anywhere within the `app` folder, including by adding new files and classes.

## Getting started

It is necessary to make some design decisions about the additional module's architecture. We strongly recommend you begin by skimming each of the tasks, not just your own, to understand how the module will come together. Then, you should discuss the architecture as a group, considering questions such as:
- How will we abstractly represent each attachment?
- How will these attachments be stored, both in memory and on disk?
- What features are bottlenecks? In other words, which members of the group must finish certain aspects of their task before others can begin?
- Are any aspects of the design important to allow you to complete your task efficiently (in terms of programming time, runtime, or memory usage)? You may need to negotiate some trade-offs with other members here.

We encourage you to try to reach a consensus about design within the first 45 minutes of the hackathon, leaving you 3 hours for programming and 15 minutes for managing last-minute Git issues.

## Git and submission notes

As a group, please make a single fork of the hackathon repo. When forking, please leave the project name and slug unchanged, and set the visibility to private.

In the GitLab web view for your fork, go to **Manage > Members** in the sidebar. Check that the COMP2100 marker bot has been added (let staff know if it hasn't), and add the accounts of each of the other group members. You will want to give them Maintainer permissions, so that they can freely push to the repo.

Each group member should then clone this fork to their personal device to work on the hackathon. You will need to set up your clone in IntelliJ by setting the SDK and importing JUnit, like in the first part of the miniproject.

You may use Git branches as you please throughout your group, but assessment will be based purely on the final commit to the `main` branch before the deadline. Git etiquette will not be considered during marking.

Your code must compile to receive marks.

## Task 1: image attachments

[20%] Each post can have up to one attachment, set or updated using the method `Post.setAttachment()`. You should choose how to store these attachments in memory and implement the functions `setAttachment` and `getAttachment` accordingly.

[20%] When a user creates an Image attachment (that is, at the point in time when the constructor to Image is called), you should save this image to disk; you may choose how images are stored on disk as long as you do not overwrite existing images. This will be important so that you can read the image back from the disk in the following part of the task.

Persistent data is currently written to a directory called `saved` by `ComputerIOFactory`. We recommend that you write images to the subdirectory `saved/img`, but this is not a requirement, as long as when the program is run multiple times from the same location, the files are written to the same place.

Note that a single instance of the Image class may be used as an attachment to multiple Posts; you should not store the save image multiple times to save on disk space. In particular, this is done by creating a single instance of Image (call it `x`) and then calling `setAttachment(x)` on multiple posts.

[60%] You are also responsible for persisting image storage, so that the image contents of an Image, as well as its caption, are invariant across multiple runs of the program.

## Task 2: poll and quiz attachments

[40%] Complete `ChoiceAttachmentFactory` using the factory design pattern to output either a Poll or a Quiz. The classes Poll and Quiz have already been created, but you may want to add to what is already there. Raise an exception if any part of the following specification is violated.

Both polls and quizzes have a question text. Both types of attachment have at least two response options, which are numbered starting at 0, but there is no maximum number of responses. Exactly one of the provided options is the single correct answer for a quiz; if the provided value does not refer to a valid option, make a poll instead.

[60%] You are also responsible for persisting polls and quizzes. You must store the question text, option labels, the correct answer (for quizzes), and all responses made by users and reload this data across multiple runs of the program.

## Task 3: large-scale data structures

[30%] Complete the `makeSelection(User user, int optionIndex)` function in Poll and Quiz:
- if this is a quiz and the person has already made a selection, raise an exception
- if this is a poll and optionIndex is outside the range of valid responses, remove the user's current selection
- otherwise, store the person's selection as selecting response `optionIndex`, where the options are numbered starting at zero

You will need to decide on which data structure(s) to use to store responses.

[30%] Complete the `getResult(int optionIndex)` function in Poll and Quiz. This function returns the fraction of respondents who chose this option, between zero (if no-one chose it) and one (if all respondents chose it).
If given invalid input, ensure that the function returns gracefully and does not raise an Exception in any situation.

You may assume that makeSelection and getResult will only be called on instances of Poll and Quiz that have already been attached to a Post.

[40%] We need to be able to support large numbers of respondents, as market research suggests that polls on Twitter have had more than 10,000,000 responses. Even at this scale, ensure that the above functionality is fast (a call to makeSelection or getResult should take at most 0.01 seconds). Your mark for this criterion cannot be higher than the sum of the other two criteria.

## Task 4: unit testing

Using JUnit, write a set of test cases for the `makeSelection` and `getResult` functions in both Poll and Quiz in the file `test/ChoiceAttachmentTests`.

[30%] You must write white-box test cases with 100% condition coverage.

[40%] Your test cases should fail on a number of buggy implementations. Because the definition of getResult is underspecified, note that there is no requirement for your test cases to pass on correct implementations other than your team's implementation.

[30%] The total number of calls from your test cases to any of `Poll.makeSelection`, `Poll.getResult`, `Quiz.makeSelection`, and `Quiz.getResult` should be at most twenty. You may need to coordinate with the team member responsible for implementing these functions to ensure that this requirement is achievable. Your mark for this criterion cannot be higher than for the first criterion (so writing no test cases will attract zero marks).

## Task 5: code quality

The work done in this hackathon is only the start of attachment support. You can imagine that we may want to add support for other kinds of attachments in future. These kinds of features are also often major sources of bugs. Accordingly, it is particularly important to make sure our code is written well.

Your job is to guarantee that code quality is consistently high throughout the project. In particular, ensure
- classes and functions are well-documented through naming conventions and comments.
- exceptions use appropriate classes and messages.
- architectural choices such as the class hierarchy are appropriate and extensible.
- the code is free of any code smells.
- algorithms execute in a reasonable amount of time and space.

[40%] We have provided a class called `QuizMarker`. Currently, this class displays very low code quality. Refactor this class to improve its quality as per the above requirements. You should modify its functionality only when fixing what you consider a bug. Also, please don't rename or move the QuizMarker class, so that we can find it during marking. That said, you may rename, add, or remove fields and methods within the class, and refactor its functionality to multiple classes if you decide this would improve the code quality.

[30%] Ensure any code written by your team during this hackathon is also high-quality, as per the above requirements. You might use methodologies like pair programming or code review to complete this.

[30%] Within the file `solid-questions.txt`, answer the following two questions in at most 75 words each:
- Explain one example of how your team's code follows the Liskov substitution principle.
- Explain one example of how your team's code follows the dependency inversion principle.

## Group task: UML

Produce a UML diagram illustrating the architecture of how your team has implemented attachments.

[50%] At minimum, your UML diagram should include at least
- five classes, including the Image, Poll, and Quiz class
- one private, one protected, and one public field
- two static and two non-static methods
- one example of each of composition, aggregation, and association

[50%] Your UML diagram should be informative and useful to a reader who wants to understand the architecture of your project. This means the selection of features should be wide enough to show the reader the overall architecture but narrow enough that irrelevancies are excluded, and they should be arranged well.

You can either draw your diagram on paper and take a photo, or use online tools and take a screenshot. Once you've finished, upload this photo or screenshot to your Git fork by replacing the file `uml.png`.