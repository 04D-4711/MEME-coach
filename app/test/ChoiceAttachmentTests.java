package test;

import attachments.Poll;
import dao.model.Post;
import dao.model.User;

import java.util.UUID;

class ChoiceAttachmentTests {
    // TODO: task 4
    private static User user() {
        return new User(UUID.randomUUID(), User.Role.Member, "tester", "pass");
    }

    private static User userWithoutId() {
        return new User(null, User.Role.Member, "missing-id", "pass");
    }

    private static Poll attachedPoll(String question) {
        Poll poll = new Poll(question, new String[]{"A", "B"});
        Post post = new Post(UUID.randomUUID());
        post.setAttachment(poll);
        return poll;
    }


    @Test
    public void pollSelectionAndResultConditions() {
        Poll poll = attachedPoll("Choose one");
        User first = user();
        User second = user();
        User neverSelected = user();

        // Valid first selections, then replacing an existing selection.
        poll.makeSelection(first, 0);
        poll.makeSelection(second, 1);
        poll.makeSelection(first, 1);
        assertEquals(1.0f, poll.getResult(1), 0.0001f);

        // An invalid selection removes an existing vote.
        poll.makeSelection(first, -1);
        assertEquals(1.0f, poll.getResult(1), 0.0001f);

        // An invalid selection from a user with no current vote changes nothing.
        poll.makeSelection(neverSelected, 2);

        // Invalid result indices must return safely; no responses return zero.
        assertEquals(0.0f, poll.getResult(-1), 0.0001f);
        assertEquals(0.0f, poll.getResult(2), 0.0001f);
        Poll emptyPoll = attachedPoll("No responses yet");
        assertEquals(0.0f, emptyPoll.getResult(0), 0.0001f);

        // Exercise both sides of the user/id validation condition.
        assertThrows(IllegalArgumentException.class, () -> poll.makeSelection(null, 0));
        assertThrows(IllegalArgumentException.class, () -> poll.makeSelection(userWithoutId(), 0));
    }
}