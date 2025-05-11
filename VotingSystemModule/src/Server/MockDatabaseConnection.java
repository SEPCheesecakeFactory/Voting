package Server;

import Common.*;

import java.util.*;

public class MockDatabaseConnection implements DatabaseConnector {

    private final Map<Integer, Poll> polls = new HashMap<>();
    private final Map<Integer, Vote> votes = new HashMap<>();
    private final Map<String, Profile> profiles = new HashMap<>();

    @Override
    public void storeVote(Vote vote) {
        votes.put(vote.getUserId(), vote);
    }

    @Override
    public void editVote(Vote vote) {
        votes.put(vote.getUserId(), vote);
    }

    @Override
    public Poll retrievePoll(int id) {
        return polls.get(id);
    }
    

    public boolean profileExists(String username) {
        return profiles.containsKey(username);
    }

    public void clear() {
        polls.clear();
        votes.clear();
        profiles.clear();
    }

    @Override
    public PollResult retrievePollResults(int id) {
        Poll poll = polls.get(id);
        if (poll == null) {
            return null;
        }
        Map<Integer, Integer> choiceVoters = new HashMap<>();
        for (Question question : poll.getQuestions()) {
            for (ChoiceOption option : question.getChoiceOptions()) {
                choiceVoters.put(option.getId(), 0); // Initialize with 0 votes
            }
        }
        for (Vote vote : votes.values()) {
            for (int choiceId : vote.getChoices()) {
                choiceVoters.put(choiceId, choiceVoters.getOrDefault(choiceId, 0) + 1);
            }
        }
        return new PollResult(poll, choiceVoters);
    }

    @Override
    public int loginOrRegisterAProfile(Profile profile) {
        if (profiles.containsKey(profile.getUsername())) {
            return profile.getId(); // Assume Profile ID is set externally
        }
        profiles.put(profile.getUsername(), profile);
        return profile.getId();
    }

    @Override
    public void changeUsername(Profile profile) {
        if (!profiles.containsKey(profile.getUsername())) {
            throw new IllegalArgumentException("Profile does not exist.");
        }
        profiles.put(profile.getUsername(), profile);
    }

    @Override
    public void storePoll(Poll poll) {
        polls.put(poll.getId(), poll);
    }

    @Override
    public boolean userHasAccessToPoll(int userId, int pollId) {
        return polls.containsKey(pollId); // Simplified access check
    }

    @Override public void closePollAndSaveResults(int pollId)
    {

    }

    @Override public boolean isOwner(int userId, int pollId)
    {
        return false;
    }
}
