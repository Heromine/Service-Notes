package notes.service.com.servicenotes;

import android.os.Bundle;

import com.github.paolorotolo.gitty_reporter.GittyReporter;

public class Gitty extends GittyReporter {
    // Please DO NOT override onCreate. Use init instead.

    @Override
    public void init(Bundle savedInstanceState) {

        // Set where Gitty will send issues.
        // (username, repository name);
        setTargetRepository("Heromine", "Service-Notes");

        // Set Auth token to open issues if user doesn't have a GitHub account
        // For example, you can register a bot account on GitHub that will open bugs for you.
        setGuestOAuth2Token("c05fa0cf62ade3764d3ecbb8d76a32a7d9c1e947");


        // OPTIONAL METHODS

        // Set if User can send bugs with his own GitHub account (default: true)
        // If false, Gitty will always use your Auth token
        enableUserGitHubLogin(true);

        // Set if Gitty can use your Auth token for users without a GitHub account (default: true)
        // If false, Gitty will redirect non registred users to github.com/join
        enableGuestGitHubLogin(true);

        // Include other relevant info in your bug report (like custom variables).
        setExtraInfo("Sent from Service Note");

        //setBackgroundColor1(getResources().getColor(R.color.colorPrimary));
        //setBackgroundColor2(getResources().getColor(R.color.colorPrimaryDark));
    }
}