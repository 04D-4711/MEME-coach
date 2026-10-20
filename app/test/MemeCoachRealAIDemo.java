package test;

import attachments.Quiz;
import dao.PostDAO;
import dao.model.Message;
import dao.model.Post;
import dao.model.User;
import memecoach.*;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class MemeCoachRealAIDemo {

    public static void main(String[] args) {
        String question = "What does DAO stand for?";
        Quiz quiz = new Quiz(question,
                new String[]{"Data Analysis Object", "Data Access Object", "Data Application Option"},
                1);
        User user = new User(UUID.randomUUID(), User.Role.Member, "testuser", "pass");

        FeedbackService fallback = new TemplateFeedbackService(new Random(7));
        LLMClient httpClient = new HttpLLMClient("http://127.0.0.1:8000/api/explain");

        AtomicReference<String> actualAIReply = new AtomicReference<>();
        LLMClient checkingClient = (systemPrompt, userPrompt) -> {
            String answer = httpClient.requestExplanation(systemPrompt, userPrompt);
            actualAIReply.set(answer);
            return answer;
        };
        FeedbackService aiService = new LLMFeedbackService(checkingClient, fallback);

        MemeCoachManager manager = new MemeCoachManager(
                aiService,
                new DemoExplanationProvider(Map.of(question,
                        "DAO separates data access from business logic.")),
                Runnable::run,
                Runnable::run);

        AtomicReference<QuizFeedback> result = new AtomicReference<>();
        AtomicReference<String> error = new AtomicReference<>();
        manager.submitAnswer(quiz, user, 0, "penguin", new FeedbackCallback() {
            @Override
            public void onCorrect() {
                error.set("Expected a wrong answer, but onCorrect was called");
            }

            @Override
            public void onFeedback(QuizFeedback feedback) {
                result.set(feedback);
            }

            @Override
            public void onError(String message) {
                error.set(message);
            }
        });

        if (error.get() != null) {
            throw new IllegalStateException("Manager error: " + error.get());
        }
        QuizFeedback feedback = result.get();
        if (feedback == null) {
            throw new IllegalStateException("No feedback received");
        }
        if (actualAIReply.get() == null
                || !actualAIReply.get().trim().equals(feedback.explanation())) {
            throw new IllegalStateException("Real AI was not used; check the backend and fallback");
        }

        System.out.println("=== REAL AI MEME FEEDBACK ===");
        System.out.println("Persona: " + feedback.personaName());
        System.out.println("Image: " + feedback.imageKey());
        System.out.println("Meme: " + feedback.headline());
        System.out.println("Correct answer: " + feedback.correctAnswer());
        System.out.println("AI explanation: " + feedback.explanation());

        Post post = manager.shareFeedback(user, feedback);
        if (PostDAO.getInstance().get(new Post(post.id)) == null) {
            throw new IllegalStateException("Shared post was not added to PostDAO");
        }
        if (!post.messages.getAll().hasNext()) {
            throw new IllegalStateException("Shared post has no message");
        }
        Message message = post.messages.getAll().next();
        if (!feedback.shareText().equals(message.message())) {
            throw new IllegalStateException("Shared post content does not match feedback");
        }

        System.out.println("Shared post ID: " + post.id);
        System.out.println("SUCCESS: Quiz -> AI -> Meme Feedback -> PostDAO");
    }
}
