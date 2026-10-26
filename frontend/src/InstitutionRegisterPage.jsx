import { useState } from "react";

const API =
  import.meta.env.VITE_API_URL ||
  "http://localhost:8080";

function InstitutionRegisterPage({ onBack }) {

  const [name, setName] =
    useState("");

  const [registrationNumber, setRegistrationNumber] =
    useState("");

  const [email, setEmail] =
    useState("");

  const [address, setAddress] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState("");

  const [success, setSuccess] =
    useState(false);

  async function submit(event) {

    event.preventDefault();

    setError("");
    setSuccess(false);
    setLoading(true);

    try {

      const response =
        await fetch(
          `${API}/api/public/institutions`,
          {
            method: "POST",

            headers: {
              "Content-Type":
                "application/json"
            },

            body: JSON.stringify({
              name: name.trim(),
              registrationNumber:
                registrationNumber.trim(),
              email: email.trim(),
              address: address.trim()
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
          "Unable to submit institution application."
        );
      }

      setSuccess(true);

      setName("");
      setRegistrationNumber("");
      setEmail("");
      setAddress("");

    } catch (error) {

      setError(error.message);

    } finally {

      setLoading(false);

    }
  }

  if (success) {

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

          <div className="success-icon">
            ✓
          </div>

          <div className="eyebrow">
            APPLICATION SUBMITTED
          </div>

          <h1>
            Institution submitted
          </h1>

          <p>
            Your institution application has
            been submitted successfully.
          </p>

          <div className="success">
            Status: PENDING ADMIN APPROVAL
          </div>

          <p>
            Once an administrator approves
            the institution, it will become
            available on the SecureCert platform.
          </p>

          <button
            className="primary"
            onClick={onBack}
          >
            Back to sign in
          </button>

        </div>

      </div>
    );
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
          INSTITUTION REGISTRATION
        </div>

        <h1>
          Register your institution
        </h1>

        <p>
          Submit your institution for
          administrator approval.
        </p>

        <form onSubmit={submit}>

          <label>
            Institution name
          </label>

          <input
            type="text"
            placeholder="Your institution name"
            value={name}
            onChange={(event) =>
              setName(event.target.value)
            }
            maxLength={200}
            required
          />

          <label>
            Registration number
          </label>

          <input
            type="text"
            placeholder="Official registration number"
            value={registrationNumber}
            onChange={(event) =>
              setRegistrationNumber(
                event.target.value
              )
            }
            maxLength={100}
            required
          />

          <label>
            Official email
          </label>

          <input
            type="email"
            placeholder="admin@institution.edu"
            value={email}
            onChange={(event) =>
              setEmail(event.target.value)
            }
            maxLength={320}
            required
          />

          <label>
            Address
          </label>

          <textarea
            rows="4"
            placeholder="Institution address"
            value={address}
            onChange={(event) =>
              setAddress(event.target.value)
            }
            maxLength={500}
            required
          />

          {error && (
            <div className="error">
              {error}
            </div>
          )}

          <button
            type="submit"
            className="primary"
            disabled={loading}
          >
            {loading
              ? "Submitting..."
              : "Submit Institution"}
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
        Institution verification · Admin approval
      </div>

    </div>
  );
}

export default InstitutionRegisterPage;