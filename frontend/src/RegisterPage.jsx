import { useState } from "react";

const API = "http://localhost:8080";

function RegisterPage({ onBack }) {

  const [fullName, setFullName] =
    useState("");

  const [email, setEmail] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [confirmPassword, setConfirmPassword] =
    useState("");

  const [error, setError] =
    useState("");

  const [success, setSuccess] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  async function register(event) {

    event.preventDefault();

    setError("");
    setSuccess("");

    if (password.length < 12) {

      setError(
        "Password must contain at least 12 characters."
      );

      return;
    }

    if (password.length > 72) {

      setError(
        "Password must not exceed 72 characters."
      );

      return;
    }

    if (password !== confirmPassword) {

      setError(
        "Passwords do not match."
      );

      return;
    }

    setLoading(true);

    try {

      const response =
        await fetch(
          `${API}/api/auth/register`,
          {
            method: "POST",

            headers: {
              "Content-Type":
                "application/json"
            },

            body: JSON.stringify({
              fullName: fullName.trim(),
              email: email.trim(),
              password
            })
          }
        );

      let data = {};

      try {
        data = await response.json();
      } catch {
        data = {};
      }

      if (!response.ok) {

        throw new Error(
          data.message ||
          "Registration failed."
        );
      }

      setSuccess(
        "Account created successfully. You can now sign in."
      );

      setFullName("");
      setEmail("");
      setPassword("");
      setConfirmPassword("");

    } catch (error) {

      setError(error.message);

    } finally {

      setLoading(false);

    }
  }

  return (
    <div className="login-page">

      <div className="login-brand">

        <div className="logo">
          S
        </div>

        <div>
          <strong>
            SecureCert
          </strong>

          <span>
            Digital Certificate Platform
          </span>
        </div>

      </div>

      <div className="login-card">

        <div className="eyebrow">
          STUDENT REGISTRATION
        </div>

        <h1>
          Create your account
        </h1>

        <p>
          Register yourself to access
          your digital certificates.
        </p>

        <form
          onSubmit={register}
        >

          <label>
            Full name
          </label>

          <input
            type="text"
            placeholder="Your full name"
            value={fullName}
            onChange={(event) =>
              setFullName(
                event.target.value
              )
            }
            required
          />

          <label>
            Email
          </label>

          <input
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) =>
              setEmail(
                event.target.value
              )
            }
            required
          />

          <label>
            Password
          </label>

          <input
            type="password"
            placeholder="Minimum 12 characters"
            value={password}
            onChange={(event) =>
              setPassword(
                event.target.value
              )
            }
            minLength={12}
            maxLength={72}
            required
          />

          <label>
            Confirm password
          </label>

          <input
            type="password"
            placeholder="Repeat your password"
            value={confirmPassword}
            onChange={(event) =>
              setConfirmPassword(
                event.target.value
              )
            }
            minLength={12}
            maxLength={72}
            required
          />

          {error && (
            <div className="error">
              {error}
            </div>
          )}

          {success && (
            <div className="success">
              {success}
            </div>
          )}

          <button
            className="primary"
            disabled={loading}
          >
            {loading
              ? "Creating account..."
              : "Create Student Account"}
          </button>

        </form>

        <button
          className="text-button"
          onClick={onBack}
        >
          ← Back to sign in
        </button>

      </div>

      <div className="login-footer">
        Secure registration · JWT protected platform
      </div>

    </div>
  );
}

export default RegisterPage;