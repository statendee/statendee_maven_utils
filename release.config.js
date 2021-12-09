module.exports = {
  plugins: [
    [
      "@semantic-release/commit-analyzer",
      {
        preset: "angular",
        releaseRules: [
          {
            breaking: true,
            release: "minor",
          },
        ],
      },
    ],
    "@semantic-release/release-notes-generator",
    "@semantic-release/github",
    [
      "@semantic-release/exec",
      {
        prepareCmd:
          "bash ./bumpVersion.sh ${nextRelease.version}",
      },
    ],
    [
      "@semantic-release/git",
      {
        assets: [["pom.xml"]],
        message: "release: ${nextRelease.version}",
      },
    ],
  ],
};