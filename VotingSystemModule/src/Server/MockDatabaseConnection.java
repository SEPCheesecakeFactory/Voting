package Server;

import Common.*;

import java.util.*;

public class MockDatabaseConnection implements DatabaseConnector {

    public final Map<Integer, Poll> polls = new HashMap<>();
    public final Map<Integer, Vote> votes = new HashMap<>();
    public final Map<Integer, Profile> profiles = new HashMap<>();
    public final Map<Integer, Integer> pollOwners = new HashMap<>();

    private int nextProfileId = 1;

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
        // Iterate through profiles to check if any username matches
        return profiles.values().stream()
            .anyMatch(profile -> profile.getUsername().equals(username));
    }

    public void clear() {
        polls.clear();
        votes.clear();
        profiles.clear();
        nextProfileId = 1;
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
                choiceVoters.put(option.getId(), 0);
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
        // First, check if the username already exists in the profiles
        for (Map.Entry<Integer, Profile> entry : profiles.entrySet()) {
            if (entry.getValue().getUsername().equals(profile.getUsername())) {
                // If profile exists, return its existing ID
                return entry.getKey();
            }
        }

        // If the profile doesn't exist, assign a new ID
        int id = nextProfileId++;
        profile.setId(id);
        profiles.put(id, profile);
        return id;
    }

    @Override
    public void changeUsername(Profile profile) {
        int id = profile.getId();
        String newUsername = profile.getUsername();

        // Check if the profile exists
        if (!profiles.containsKey(id)) {
            throw new IllegalArgumentException("Profile with ID " + id + " does not exist.");
        }

        // Check if the new username already exists in the map
        for (Profile existingProfile : profiles.values()) {
            if (existingProfile.getUsername().equals(newUsername)) {
                throw new IllegalArgumentException("Username '" + newUsername + "' already exists.");
            }
        }

        // If no conflict, update the profile
        profiles.put(id, profile);
    }


    @Override
    public Poll storePoll(Poll poll, Profile profile) {
        polls.put(poll.getId(), poll);
        return poll;
    }

    @Override
    public boolean userHasAccessToPoll(int userId, int pollId) {
        return polls.containsKey(pollId);
    }

    @Override
    public void closePollAndSaveResults(int pollId) {
        // No-op for mock
    }

    @Override
    public boolean isOwner(int userId, int pollId) {
        // Check if the given userId is the owner of the poll
        return pollOwners.getOrDefault(pollId, -1) == userId;
    }

    @Override public void addUserToGroup(int userId, int groupId)
    {

    }

  @Override public int createUserGroup(String groupName, int creatorId)
  {
    return 0;
  }





  @Override public Profile getProfileByUsername(String username)
  {
    return null;
  }

  @Override public UserGroup getGroupByUsername(String username)
  {
    return null;
  }

  @Override public void grantPollAccessToUser(int pollId, int userId, int clientId)
  {

  }

    @Override public void grantPollAccessToGroup(int pollId, String groupName, int clientId)
    {

    }

    @Override
    public List<Poll> getAllAvailablePolls(int userId) {
        List<Poll> availablePolls = new ArrayList<>();
        for (Poll poll : polls.values()) {
            if (!poll.isClosed()) {
                availablePolls.add(poll);
            }
        }
        return availablePolls;
    }

  @Override public List<UserGroup> getGroupsCreatedByUser(int userId)
  {
    return List.of();
  }

}
