import { useEffect, useState } from "react";

const API =
  import.meta.env.VITE_API_URL ||
  "http://localhost:8080";

function RegisterPage({ onBack }) {

  const [institutions, setInstitutions] =
    useState([]);

  const [institutionId, setInstitutionId] =
    useState("");

  const [fullName, setFullName] =
    useState("");

  const [email, setEmail] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [confirmPassword, setConfirmPassword] =
    useState("");

  const [loadingInstitutions, setLoadingInstitutions] =
    useState(true);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState("");

  const [success, setSuccess] =
    useState("");

  useEffect(() => {

    async function loadInstitutions() {

      try {

        const response =
          await fetch(
            `${API}/api/public/institutions`
          );

        const data =
          await response.json();

        if (!response.ok) {

          throw new Error(
            data.message ||
            "Unable to load institutions."
          );
        }

        const activeInstitutions =
          Array.isArray(data)
            ? data
            : [];

        setInstitutions(
          activeInstitutions
        );

        if (
          activeInstitutions.length > 0
        ) {

          setInstitutionId(
            activeInstitutions[0].id
          );
        }

      } catch (error) {

        setError(error.message);

      } finally {

        setLoadingInstitutions(false);

      }
    }

    loadInstitutions();

  }, []);

  async function register(event) {

    event.preventDefault();

    setError("");
    setSuccess("");

    if (!institutionId) {

      setError(
        "Please select an institution."
      );

      return;
    }

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
              fullName:
                fullName.trim(),

              email:
                email.trim(),

              password,

              institutionId
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

      /*
       * The backend returns an AuthResponse,
       * so registration also logs the user in.
       */
      localStorage.setItem(
        "token",
        data.accessToken
      );

      localStorage.setItem(
        "user",
        JSON.stringify({
          userId: data.userId,
          fullName: data.fullName,
          email: data.email,
          role: data.role
        })
      );

      setSuccess(
        "Account created successfully."
      );

      /*
       * Give the browser a moment to show
       * the success message, then open dashboard.
       */
      setTimeout(() => {
        window.location.href = "/";
      }, 700);

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
          Register yourself and start
          managing your digital credentials.
        </p>

        <form onSubmit={register}>

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
            maxLength={150}
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
            maxLength={320}
            required
          />

          <label>
            Institution
          </label>

          {loadingInstitutions ? (

            <div className="loading-field">
              Loading institutions...
            </div>

          ) : institutions.length === 0 ? (

            <div className="error">
              No active institutions are
              currently available for registration.
            </div>

          ) : (

            <select
              value={institutionId}
              onChange={(event) =>
                setInstitutionId(
                  event.target.value
                )
              }
              required
            >

              {institutions.map(
                (institution) => (

                  <option
                    key={institution.id}
                    value={institution.id}
                  >
                    {institution.name}
                  </option>

                )
              )}

            </select>

          )}

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
            type="submit"
            className="primary"
            disabled={
              loading ||
              loadingInstitutions ||
              institutions.length === 0
            }
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
        Secure registration · Institution verified
      </div>

    </div>
  );
}

export default RegisterPage;