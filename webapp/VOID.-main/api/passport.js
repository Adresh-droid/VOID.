/**
 * api/passport.js
 * Passport.js configuration — Google OAuth 2.0 strategy.
 */

'use strict';

const passport = require('passport');
const { Strategy: GoogleStrategy } = require('passport-google-oauth20');
const config = require('../config');

// We store the full user object in the session (no DB needed).
// For a production app with many users, you'd store only the user ID
// and look up from a DB in deserializeUser.
passport.serializeUser((user, done) => done(null, user));
passport.deserializeUser((user, done) => done(null, user));

passport.use(new GoogleStrategy(
  {
    clientID:     config.google.clientId,
    clientSecret: config.google.clientSecret,
    callbackURL:  config.google.callbackUrl,
  },
  (accessToken, refreshToken, profile, done) => {
    const user = {
      id:           profile.id,
      displayName:  profile.displayName,
      email:        profile.emails?.[0]?.value || '',
      photo:        profile.photos?.[0]?.value || '',
      accessToken,
      refreshToken, // may be undefined if user has already consented; stored from first login
    };
    done(null, user);
  }
));

module.exports = passport;
