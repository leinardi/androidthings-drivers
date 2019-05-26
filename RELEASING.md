# Releasing

1. Bump the `mvn_config.version` property in `mavenConfig.gradle` based on Major.Minor.Patch naming scheme
2. Update `CHANGELOG.md` for the impending release.
3. Update the `README.md` with the new version.
4. `git commit -am "Prepare for release X.Y.Z"` (where X.Y.Z is the version you set in step 1)
5. `git push`
6. `./gradlew clean assembleRelease bintrayUpload`
7. Create a PR from [master](../../tree/master) to [release](../../tree/release)
8. Visit [JFrog Bintray](https://bintray.com/leinardi/androidthings) and promote the artifacts
