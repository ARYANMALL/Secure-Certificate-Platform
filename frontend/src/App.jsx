import { useEffect, useState } from "react";
import "./App.css";

import RegisterPage from "./RegisterPage";
import InstitutionRegisterPage from "./InstitutionRegisterPage";
import IssuerIssueCertificate from "./IssuerIssueCertificate";

const API =
  import.meta.env.VITE_API_URL ||
  "http://localhost:8080";

/* =========================================================
   API HELPERS
   ========================================================= */

async function requestJson(url, options = {}) {
  const response = await fetch(url, options);

  if (!response.ok) {
    let message = `Request failed (${response.status})`;

    try {
      const errorData = await response.json();

      message =
        errorData.message ||
        errorData.error ||
        message;
    } catch {
      // Response did not contain JSON.
    }

    throw new Error(message);
  }

  try {
    return await response.json();
  } catch {
    return {};
  }
}

function authHeaders(token) {
  return {
    Authorization: `Bearer ${token}`
  };
}

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);

  const link = document.createElement("a");

  link.href = url;
  link.download = filename;

  document.body.appendChild(link);

  link.click();

  link.remove();

  URL.revokeObjectURL(url);
}

function formatDate(value) {
  if (!value) {
    return "";
  }

  try {
    return new Date(value).toLocaleDateString();
  } catch {
    return value;
  }
}

/* =========================================================
   APP
   ========================================================= */

function App() {
  const path = window.location.pathname;

  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(
        localStorage.getItem("user") || "null"
      );
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState(
    localStorage.getItem("token")
  );

  /* =======================================================
     PUBLIC STUDENT REGISTRATION
     ======================================================= */

  if (path === "/register") {
    return (
      <RegisterPage
        onBack={() => {
          window.location.href = "/";
        }}
      />
    );
  }

  /* =======================================================
     PUBLIC INSTITUTION REGISTRATION
     ======================================================= */

  if (path === "/register/institution") {
    return (
      <InstitutionRegisterPage
        onBack={() => {
          window.location.href = "/";
        }}
      />
    );
  }

  /* =======================================================
     PUBLIC CERTIFICATE VERIFICATION
     ======================================================= */

  if (path === "/verify") {
    return (
      <VerifyPage
        initialNumber=""
        onBack={() => {
          window.location.href = "/";
        }}
      />
    );
  }

  if (path.startsWith("/verify/")) {
    const certificateNumber = decodeURIComponent(
      path.substring("/verify/".length)
    );

    return (
      <VerifyPage
        initialNumber={certificateNumber}
        onBack={() => {
          window.location.href = "/";
        }}
      />
    );
  }

  /* =======================================================
     LOGIN
     ======================================================= */

  if (!token || !user) {
    return (
      <LoginPage
        onLogin={(data) => {
          const loggedUser = {
            userId: data.userId,
            fullName: data.fullName,
            email: data.email,
            role: data.role
          };

          localStorage.setItem(
            "token",
            data.accessToken
          );

          localStorage.setItem(
            "user",
            JSON.stringify(loggedUser)
          );

          setToken(data.accessToken);
          setUser(loggedUser);
        }}
      />
    );
  }

  /* =======================================================
     AUTHENTICATED APPLICATION
     ======================================================= */

  return (
    <Dashboard
      user={user}
      token={token}
      onLogout={() => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");

        setToken(null);
        setUser(null);
      }}
    />
  );
}

/* =========================================================
   LOGIN PAGE
   ========================================================= */

function LoginPage({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function login(event) {
    event.preventDefault();

    setError("");
    setLoading(true);

    try {
      const data = await requestJson(
        `${API}/api/auth/login`,
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json"
          },

          body: JSON.stringify({
            email: email.trim(),
            password
          })
        }
      );

      onLogin(data);

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
          SECURE ACCESS
        </div>

        <h1>
          Welcome back
        </h1>

        <p>
          Sign in to manage and verify
          digital certificates.
        </p>

        <form onSubmit={login}>

          <label>
            Email
          </label>

          <input
            type="email"
            value={email}
            placeholder="you@example.com"
            onChange={(event) =>
              setEmail(event.target.value)
            }
            required
          />

          <label>
            Password
          </label>

          <input
            type="password"
            value={password}
            placeholder="Your password"
            onChange={(event) =>
              setPassword(event.target.value)
            }
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
              ? "Signing in..."
              : "Sign in"}
          </button>

        </form>

        <button
          className="text-button"
          onClick={() =>
            window.location.href = "/register"
          }
        >
          Create a student account →
        </button>

        <button
          className="text-button"
          onClick={() =>
            window.location.href =
              "/register/institution"
          }
        >
          Register an institution →
        </button>

        <button
          className="text-button"
          onClick={() =>
            window.location.href = "/verify"
          }
        >
          Verify a certificate →
        </button>

      </div>

      <div className="login-footer">
        Secure registration · JWT authentication · RSA verification
      </div>

    </div>
  );
}

/* =========================================================
   DASHBOARD
   ========================================================= */

function Dashboard({
  user,
  token,
  onLogout
}) {
  const [page, setPage] = useState(
    user.role === "STUDENT"
      ? "student"
      : "overview"
  );

  function getPageTitle() {
    switch (page) {

      case "student":
        return "My Certificates";

      case "student-request":
        return "Request Certificate";

      case "issuer-requests":
        return "Certificate Requests";

      case "issuer-issue":
        return "Issue Certificate";

      case "issuer-certificates":
        return "Issued Certificates";

      case "admin-institutions":
        return "Institution Management";

      case "admin-approvals":
        return "Certificate Approvals";

      default:
        return "Overview";
    }
  }

  return (
    <div className="layout">

      {/* ===================================================
          SIDEBAR
          =================================================== */}

      <aside className="sidebar">

        <div className="brand">

          <div className="logo">
            S
          </div>

          <strong>
            SecureCert
          </strong>

        </div>

        <div className="role">
          {user.role}
        </div>

        <nav>

          {/* OVERVIEW */}

          <NavButton
            active={page === "overview"}
            onClick={() =>
              setPage("overview")
            }
          >
            Overview
          </NavButton>

          {/* STUDENT */}

          {user.role === "STUDENT" && (
            <>
              <NavButton
                active={
                  page === "student-request"
                }
                onClick={() =>
                  setPage("student-request")
                }
              >
                Request Certificate
              </NavButton>

              <NavButton
                active={
                  page === "student"
                }
                onClick={() =>
                  setPage("student")
                }
              >
                My Certificates
              </NavButton>
            </>
          )}

          {/* CENTRAL ISSUER */}

          {user.role === "ISSUER" && (
            <>
              <NavButton
                active={
                  page === "issuer-requests"
                }
                onClick={() =>
                  setPage("issuer-requests")
                }
              >
                Certificate Requests
              </NavButton>

              <NavButton
                active={
                  page === "issuer-issue"
                }
                onClick={() =>
                  setPage("issuer-issue")
                }
              >
                Issue Certificate
              </NavButton>

              <NavButton
                active={
                  page === "issuer-certificates"
                }
                onClick={() =>
                  setPage("issuer-certificates")
                }
              >
                Issued Certificates
              </NavButton>
            </>
          )}

          {/* ADMIN */}

          {user.role === "ADMIN" && (
            <>
              <NavButton
                active={
                  page === "admin-institutions"
                }
                onClick={() =>
                  setPage("admin-institutions")
                }
              >
                Institutions
              </NavButton>

              <NavButton
                active={
                  page === "admin-approvals"
                }
                onClick={() =>
                  setPage("admin-approvals")
                }
              >
                Certificate Approvals
              </NavButton>
            </>
          )}

        </nav>

        {/* SIDEBAR BOTTOM */}

        <div className="sidebar-bottom">

          <div className="mini-profile">

            <div className="avatar">
              {user.fullName?.charAt(0)}
            </div>

            <div>

              <strong>
                {user.fullName}
              </strong>

              <span>
                {user.email}
              </span>

            </div>

          </div>

          <button
            className="logout"
            onClick={onLogout}
          >
            Sign out
          </button>

        </div>

      </aside>

      {/* ===================================================
          MAIN
          =================================================== */}

      <main className="main">

        <header className="header">

          <div>

            <span className="breadcrumb">
              SecureCert / Platform
            </span>

            <h1>
              {getPageTitle()}
            </h1>

          </div>

          <span className="header-role">
            {user.role}
          </span>

        </header>

        {/* OVERVIEW */}

        {page === "overview" && (
          <Overview
            user={user}
            onNavigate={setPage}
          />
        )}

        {/* STUDENT REQUEST */}

        {page === "student-request" && (
          <StudentRequestPage
            token={token}
            onSubmitted={() =>
              setPage("student")
            }
          />
        )}

        {/* STUDENT */}

        {page === "student" && (
          <StudentPage
            user={user}
            token={token}
          />
        )}

        {/* ISSUER REQUESTS */}

        {page === "issuer-requests" && (
          <IssuerRequests
            token={token}
          />
        )}

        {/* ISSUER ISSUE */}

        {page === "issuer-issue" && (
          <IssuerIssueCertificate
            token={token}
          />
        )}

        {/* ISSUER CERTIFICATES */}

        {page === "issuer-certificates" && (
          <IssuerCertificates
            token={token}
          />
        )}

        {/* ADMIN INSTITUTIONS */}

        {page === "admin-institutions" && (
          <AdminInstitutions
            token={token}
          />
        )}

        {/* ADMIN APPROVALS */}

        {page === "admin-approvals" && (
          <AdminApprovals
            token={token}
          />
        )}

      </main>

    </div>
  );
}

/* =========================================================
   NAVIGATION BUTTON
   ========================================================= */

function NavButton({
  active,
  onClick,
  children
}) {
  return (
    <button
      className={
        active
          ? "nav active"
          : "nav"
      }
      onClick={onClick}
    >
      {children}
    </button>
  );
}

/* =========================================================
   OVERVIEW
   ========================================================= */

function Overview({
  user,
  onNavigate
}) {
  return (
    <div className="content">

      <section className="welcome">

        <div>

          <span className="eyebrow">
            DIGITAL CREDENTIAL PLATFORM
          </span>

          <h2>
            Welcome, {user.fullName}
          </h2>

          <p>
            Your SecureCert workspace is ready.
          </p>

        </div>

        <div className="security-badge">
          ✓
        </div>

      </section>

      <div className="stats">

        <Stat
          title="Account"
          value="Active"
        />

        <Stat
          title="Role"
          value={user.role}
        />

        <Stat
          title="Authentication"
          value="JWT"
        />

        <Stat
          title="Security"
          value="SHA-256 + RSA"
        />

      </div>

      {/* STUDENT */}

      {user.role === "STUDENT" && (

        <section className="card">

          <span className="eyebrow">
            STUDENT ACTION
          </span>

          <h3>
            Need a certificate?
          </h3>

          <p>
            Submit a certificate request
            to your institution. The central
            issuer will review your request
            before administrator approval.
          </p>

          <button
            className="primary"
            onClick={() =>
              onNavigate(
                "student-request"
              )
            }
          >
            Apply for Certificate
          </button>

        </section>

      )}

      {/* ISSUER */}

      {user.role === "ISSUER" && (

        <section className="card">

          <span className="eyebrow">
            CENTRAL ISSUER
          </span>

          <h3>
            Manage certificates
          </h3>

          <p>
            Review certificate requests from
            every institution or manually
            prepare a certificate for any
            active student.
          </p>

          <div className="button-row">

            <button
              className="primary"
              onClick={() =>
                onNavigate(
                  "issuer-requests"
                )
              }
            >
              View Requests
            </button>

            <button
              className="secondary"
              onClick={() =>
                onNavigate(
                  "issuer-issue"
                )
              }
            >
              Issue Certificate
            </button>

          </div>

        </section>

      )}

      {/* ADMIN */}

      {user.role === "ADMIN" && (

        <section className="card">

          <span className="eyebrow">
            ADMINISTRATION
          </span>

          <h3>
            Manage the platform
          </h3>

          <p>
            Manage institutions and provide
            final approval for certificates.
          </p>

          <button
            className="primary"
            onClick={() =>
              onNavigate(
                "admin-approvals"
              )
            }
          >
            Review Approvals
          </button>

        </section>

      )}

      <section className="card">

        <span className="eyebrow">
          TRUST ARCHITECTURE
        </span>

        <h3>
          Certificate security
        </h3>

        <p>
          Approved certificates are protected
          with SHA-256 integrity hashing and
          RSA digital signatures.
        </p>

        <div className="flow">

          <span>
            Student
          </span>

          <b>→</b>

          <span>
            Issuer
          </span>

          <b>→</b>

          <span>
            Admin
          </span>

          <b>→</b>

          <span>
            SHA-256
          </span>

          <b>→</b>

          <span>
            RSA
          </span>

        </div>

      </section>

    </div>
  );
}

function Stat({
  title,
  value
}) {
  return (
    <div className="stat">

      <span>
        {title}
      </span>

      <strong>
        {value}
      </strong>

    </div>
  );
}

/* =========================================================
   STUDENT REQUEST PAGE
   ========================================================= */

function StudentRequestPage({
  token,
  onSubmitted
}) {
  const [courseName, setCourseName] =
    useState("");

  const [description, setDescription] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState("");

  const [message, setMessage] =
    useState("");

  async function submitRequest(event) {

    event.preventDefault();

    setError("");
    setMessage("");
    setLoading(true);

    try {

      await requestJson(
        `${API}/api/certificate-requests`,
        {
          method: "POST",

          headers: {
            ...authHeaders(token),
            "Content-Type":
              "application/json"
          },

          body: JSON.stringify({
            courseName:
              courseName.trim(),

            description:
              description.trim()
          })
        }
      );

      setMessage(
        "Certificate request submitted successfully."
      );

      setCourseName("");
      setDescription("");

      setTimeout(() => {
        onSubmitted();
      }, 800);

    } catch (error) {

      setError(error.message);

    } finally {

      setLoading(false);

    }
  }

  return (
    <div className="content">

      <section className="card">

        <span className="eyebrow">
          CERTIFICATE REQUEST
        </span>

        <h3>
          Apply for a certificate
        </h3>

        <p>
          Submit your request to your
          institution. An authorized issuer
          will review it before administrator
          approval.
        </p>

        <form
          className="form"
          onSubmit={submitRequest}
        >

          <label>
            Course / Program
          </label>

          <input
            type="text"
            value={courseName}
            placeholder="Java Full Stack Development"
            onChange={(event) =>
              setCourseName(
                event.target.value
              )
            }
            maxLength={200}
            required
          />

          <label>
            Description
          </label>

          <textarea
            rows="6"
            value={description}
            placeholder="Describe the course or achievement."
            onChange={(event) =>
              setDescription(
                event.target.value
              )
            }
            maxLength={2000}
          />

          {error && (
            <div className="error">
              {error}
            </div>
          )}

          {message && (
            <div className="success">
              {message}
            </div>
          )}

          <button
            type="submit"
            className="primary"
            disabled={loading}
          >
            {loading
              ? "Submitting request..."
              : "Submit Certificate Request"}
          </button>

        </form>

      </section>

      <section className="card">

        <span className="eyebrow">
          APPROVAL PROCESS
        </span>

        <div className="flow">

          <span>
            Student
          </span>

          <b>→</b>

          <span>
            Central Issuer
          </span>

          <b>→</b>

          <span>
            Admin
          </span>

          <b>→</b>

          <span>
            Certificate
          </span>

        </div>

      </section>

    </div>
  );
}

/* =========================================================
   STUDENT PAGE
   ========================================================= */

function StudentPage({
  user,
  token
}) {
  const [certificates, setCertificates] =
    useState([]);

  const [requests, setRequests] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  useEffect(() => {

    let cancelled = false;

    async function load() {

      try {

        const [
          certificateResponse,
          requestResponse
        ] = await Promise.all([

          fetch(
            `${API}/api/student/certificates`,
            {
              headers:
                authHeaders(token)
            }
          ),

          fetch(
            `${API}/api/certificate-requests/my`,
            {
              headers:
                authHeaders(token)
            }
          )

        ]);

        const certificateData =
          await certificateResponse.json();

        const requestData =
          await requestResponse.json();

        if (!certificateResponse.ok) {

          throw new Error(
            certificateData.message ||
            "Unable to load certificates."
          );
        }

        if (!requestResponse.ok) {

          throw new Error(
            requestData.message ||
            "Unable to load requests."
          );
        }

        if (!cancelled) {

          setCertificates(
            Array.isArray(certificateData)
              ? certificateData
              : []
          );

          setRequests(
            Array.isArray(requestData)
              ? requestData
              : []
          );

        }

      } catch (error) {

        if (!cancelled) {
          setError(error.message);
        }

      } finally {

        if (!cancelled) {
          setLoading(false);
        }

      }
    }

    load();

    return () => {
      cancelled = true;
    };

  }, [token]);

  async function downloadPdf(
    id,
    certificateNumber
  ) {

    try {

      const response =
        await fetch(
          `${API}/api/student/certificates/${id}/pdf`,
          {
            headers:
              authHeaders(token)
          }
        );

      if (!response.ok) {

        throw new Error(
          "Unable to download certificate."
        );
      }

      const blob =
        await response.blob();

      downloadBlob(
        blob,
        `${certificateNumber}.pdf`
      );

    } catch (error) {

      setError(error.message);

    }
  }

  return (
    <div className="content">

      {/* PROFILE */}

      <section className="card">

        <span className="eyebrow">
          STUDENT PROFILE
        </span>

        <div className="student-profile">

          <div className="large-avatar">
            {user.fullName?.charAt(0)}
          </div>

          <div>

            <h2>
              {user.fullName}
            </h2>

            <p>
              {user.email}
            </p>

            <span className="status active">
              VERIFIED STUDENT
            </span>

          </div>

        </div>

      </section>

      {error && (
        <div className="error">
          {error}
        </div>
      )}

      {/* REQUESTS */}

      <section className="card">

        <div className="card-heading">

          <div>

            <span className="eyebrow">
              REQUEST TRACKING
            </span>

            <h3>
              My Certificate Requests
            </h3>

            <p>
              Track your certificate applications.
            </p>

          </div>

          <strong>
            {requests.length}
          </strong>

        </div>

        {loading ? (

          <div className="empty">
            Loading requests...
          </div>

        ) : requests.length === 0 ? (

          <div className="empty">

            <div className="empty-icon">
              ◇
            </div>

            <p>
              No certificate requests yet.
            </p>

          </div>

        ) : (

          <div className="certificate-list">

            {requests.map(
              (request) => (

                <div
                  className="certificate-item"
                  key={request.id}
                >

                  <div className="certificate-main">

                    <div className="certificate-symbol">

                      {request.status ===
                        "COMPLETED"
                        ? "✓"
                        : request.status ===
                          "REJECTED"
                          ? "!"
                          : "○"}

                    </div>

                    <div>

                      <span className="certificate-number">
                        REQUEST
                      </span>

                      <h3>
                        {request.courseName}
                      </h3>

                      <p>
                        {request.institutionName}
                      </p>

                      <small>
                        Submitted{" "}
                        {formatDate(
                          request.createdAt
                        )}
                      </small>

                      {request.issuerName && (
                        <small>
                          {" · "}
                          Issuer:{" "}
                          {request.issuerName}
                        </small>
                      )}

                    </div>

                  </div>

                  <div className="certificate-actions">

                    <span
                      className={
                        request.status ===
                          "COMPLETED"
                          ? "status active"
                          : request.status ===
                            "REJECTED"
                            ? "status revoked"
                            : "status pending"
                      }
                    >
                      {request.status ===
                        "PENDING_ISSUER"
                        ? "AWAITING ISSUER"
                        : request.status ===
                          "PENDING_ADMIN"
                          ? "AWAITING ADMIN"
                          : request.status}
                    </span>

                    {request.certificateNumber && (

                      <button
                        className="small-button"
                        onClick={() =>
                          window.location.href =
                            `/verify/${request.certificateNumber}`
                        }
                      >
                        Verify
                      </button>

                    )}

                  </div>

                </div>

              )
            )}

          </div>

        )}

      </section>

      {/* CERTIFICATES */}

      <section className="card">

        <div className="card-heading">

          <div>

            <span className="eyebrow">
              DIGITAL CREDENTIALS
            </span>

            <h3>
              My Certificates
            </h3>

            <p>
              Approved certificates issued
              to your account.
            </p>

          </div>

          <strong>
            {certificates.length}
          </strong>

        </div>

        {loading ? (

          <div className="empty">
            Loading certificates...
          </div>

        ) : certificates.length === 0 ? (

          <div className="empty">

            <div className="empty-icon">
              ◇
            </div>

            <p>
              No certificates have been
              issued to you yet.
            </p>

          </div>

        ) : (

          <div className="certificate-list">

            {certificates.map(
              (certificate) => (

                <div
                  className="certificate-item"
                  key={certificate.id}
                >

                  <div className="certificate-main">

                    <div className="certificate-symbol">

                      {certificate.status ===
                        "ACTIVE"
                        ? "✓"
                        : "!"}

                    </div>

                    <div>

                      <span className="certificate-number">
                        {certificate.certificateNumber}
                      </span>

                      <h3>
                        {certificate.courseName}
                      </h3>

                      <p>
                        {certificate.institutionName}
                      </p>

                      <small>
                         Issued on{" "}
                         {certificate.issueDate}
                  </small>

                   {certificate.status === "REVOKED" &&
                     certificate.revocationReason && (
                 <div className="revocation-reason">
                  <strong>
                    Revocation reason
                  </strong>

                  <span>
                   {certificate.revocationReason}
                  </span>
                 </div>
                )}

                    </div>

                  </div>

                  <div className="certificate-actions">

                    <span
                      className={
                        certificate.status ===
                        "ACTIVE"
                          ? "status active"
                          : "status revoked"
                      }
                    >
                      {certificate.status}
                    </span>

                    <button
                      className="secondary"
                      onClick={() =>
                        downloadPdf(
                          certificate.id,
                          certificate.certificateNumber
                        )
                      }
                    >
                      Download PDF
                    </button>

                    <button
                      className="small-button"
                      onClick={() =>
                        window.location.href =
                          `/verify/${certificate.certificateNumber}`
                      }
                    >
                      Verify
                    </button>

                  </div>

                </div>

              )
            )}

          </div>

        )}

      </section>

    </div>
  );
}

/* =========================================================
   ISSUER REQUESTS
   ========================================================= */

function IssuerRequests({ token }) {

  const [requests, setRequests] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [message, setMessage] =
    useState("");

  useEffect(() => {

    let cancelled = false;

    async function load() {

      try {

        const data =
          await requestJson(
            `${API}/api/certificate-requests/issuer`,
            {
              headers:
                authHeaders(token)
            }
          );

        if (!cancelled) {

          setRequests(
            Array.isArray(data)
              ? data
              : []
          );

        }

      } catch (error) {

        if (!cancelled) {
          setError(error.message);
        }

      } finally {

        if (!cancelled) {
          setLoading(false);
        }

      }
    }

    load();

    return () => {
      cancelled = true;
    };

  }, [token]);

  async function acceptRequest(id) {

    setError("");
    setMessage("");

    try {

      await requestJson(
        `${API}/api/certificate-requests/${id}/accept`,
        {
          method: "PATCH",
          headers:
            authHeaders(token)
        }
      );

      setRequests((items) =>
        items.filter(
          (item) =>
            item.id !== id
        )
      );

      setMessage(
        "Request accepted and sent to administrator."
      );

    } catch (error) {

      setError(error.message);

    }
  }

  return (
    <div className="content">

      <section className="card">

        <div className="card-heading">

          <div>

            <span className="eyebrow">
              CENTRAL ISSUER
            </span>

            <h3>
              Certificate Requests
            </h3>

            <p>
              Requests from all institutions
              appear here.
            </p>

          </div>

          <strong>
            {requests.length}
          </strong>

        </div>

        {error && (
          <div className="error">
            {error}
          </div>
        )}

        {message && (
          <div className="success">
            {message}
          </div>
        )}

        {loading ? (

          <div className="empty">
            Loading requests...
          </div>

        ) : requests.length === 0 ? (

          <div className="empty">

            <div className="empty-icon">
              ✓
            </div>

            <p>
              No new certificate requests.
            </p>

          </div>

        ) : (

          <div className="certificate-list">

            {requests.map(
              (request) => (

                <div
                  className="certificate-item"
                  key={request.id}
                >

                  <div className="certificate-main">

                    <div className="certificate-symbol">
                      ○
                    </div>

                    <div>

                      <span className="certificate-number">
                        NEW REQUEST
                      </span>

                      <h3>
                        {request.courseName}
                      </h3>

                      <p>
                        {request.studentName}
                        {" · "}
                        {request.institutionName}
                      </p>

                      {request.description && (
                        <small>
                          {request.description}
                        </small>
                      )}

                      <small>
                        Submitted{" "}
                        {formatDate(
                          request.createdAt
                        )}
                      </small>

                    </div>

                  </div>

                  <div className="certificate-actions">

                    <span className="status pending">
                      AWAITING ISSUER
                    </span>

                    <button
                      className="small-button"
                      onClick={() =>
                        acceptRequest(
                          request.id
                        )
                      }
                    >
                      Accept & Prepare
                    </button>

                  </div>

                </div>

              )
            )}

          </div>

        )}

      </section>

    </div>
  );
}

/* =========================================================
   ISSUER CERTIFICATES
   ========================================================= */

function IssuerCertificates({ token }) {

  const [certificates, setCertificates] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  useEffect(() => {

    let cancelled = false;

    async function load() {

      try {

        const data =
          await requestJson(
            `${API}/api/certificates`,
            {
              headers:
                authHeaders(token)
            }
          );

        if (!cancelled) {

          setCertificates(
            Array.isArray(data)
              ? data
              : []
          );

        }

      } catch (error) {

        if (!cancelled) {
          setError(error.message);
        }

      } finally {

        if (!cancelled) {
          setLoading(false);
        }

      }
    }

    load();

    return () => {
      cancelled = true;
    };

  }, [token]);

  async function downloadPdf(
    id,
    certificateNumber
  ) {

    try {

      const response =
        await fetch(
          `${API}/api/certificates/${id}/pdf`,
          {
            headers:
              authHeaders(token)
          }
        );

      if (!response.ok) {

        throw new Error(
          "Unable to download certificate."
        );
      }

      const blob =
        await response.blob();

      downloadBlob(
        blob,
        `${certificateNumber}.pdf`
      );

    } catch (error) {

      setError(error.message);

    }
  }

  async function revoke(id) {

    const reason =
      window.prompt(
        "Enter the reason for revocation:"
      );

    if (!reason?.trim()) {
      return;
    }

    setError("");

    try {

      const data =
        await requestJson(
          `${API}/api/certificates/${id}/revoke`,
          {
            method: "PATCH",

            headers: {
              ...authHeaders(token),
              "Content-Type":
                "application/json"
            },

            body: JSON.stringify({
              reason: reason.trim()
            })
          }
        );

      setCertificates((items) =>
        items.map((item) =>
          item.id === id
            ? data
            : item
        )
      );

    } catch (error) {

      setError(error.message);

    }
  }

  return (
    <div className="content">

      <section className="card">

        <div className="card-heading">

          <div>

            <span className="eyebrow">
              CERTIFICATE MANAGEMENT
            </span>

            <h3>
              Issued Certificates
            </h3>

            <p>
              Manage certificates from every
              institution.
            </p>

          </div>

          <strong>
            {certificates.length}
          </strong>

        </div>

        {error && (
          <div className="error">
            {error}
          </div>
        )}

        {loading ? (

          <div className="empty">
            Loading certificates...
          </div>

        ) : certificates.length === 0 ? (

          <div className="empty">
            No certificates have been issued.
          </div>

        ) : (

          <div className="certificate-list">

            {certificates.map(
              (certificate) => (

                <div
                  className="certificate-item"
                  key={certificate.id}
                >

                  <div className="certificate-main">

                    <div className="certificate-symbol">
                      {certificate.status ===
                        "ACTIVE"
                        ? "✓"
                        : "!"}
                    </div>

                    <div>

                      <span className="certificate-number">
                        {certificate.certificateNumber}
                      </span>

                      <h3>
                        {certificate.courseName}
                      </h3>

                      <p>
                        {certificate.studentName}
                        {" · "}
                        {certificate.institutionName}
                      </p>

                      <small>
                        Issued on{" "}
                        {certificate.issueDate}
                      </small>

                    </div>

                  </div>

                  <div className="certificate-actions">

                    <span
                      className={
                        certificate.status ===
                        "ACTIVE"
                          ? "status active"
                          : "status revoked"
                      }
                    >
                      {certificate.status}
                    </span>

                    <button
                      className="small-button"
                      onClick={() =>
                        window.location.href =
                          `/verify/${certificate.certificateNumber}`
                      }
                    >
                      Verify
                    </button>

                    <button
                      className="small-button"
                      onClick={() =>
                        downloadPdf(
                          certificate.id,
                          certificate.certificateNumber
                        )
                      }
                    >
                      PDF
                    </button>

                    {certificate.status ===
                      "ACTIVE" && (

                      <button
                        className="danger-button"
                        onClick={() =>
                          revoke(
                            certificate.id
                          )
                        }
                      >
                        Revoke
                      </button>

                    )}

                  </div>

                </div>

              )
            )}

          </div>

        )}

      </section>

    </div>
  );
}

/* =========================================================
   ADMIN INSTITUTIONS
   ========================================================= */

function AdminInstitutions({ token }) {

  const [institutions, setInstitutions] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [message, setMessage] =
    useState("");

  useEffect(() => {

    let cancelled = false;

    async function load() {

      try {

        const data =
          await requestJson(
            `${API}/api/institutions`,
            {
              headers:
                authHeaders(token)
            }
          );

        if (!cancelled) {

          setInstitutions(
            Array.isArray(data)
              ? data
              : []
          );

        }

      } catch (error) {

        if (!cancelled) {
          setError(error.message);
        }

      } finally {

        if (!cancelled) {
          setLoading(false);
        }

      }
    }

    load();

    return () => {
      cancelled = true;
    };

  }, [token]);

  async function activate(id) {

    setError("");
    setMessage("");

    try {

      const data =
        await requestJson(
          `${API}/api/institutions/${id}/activate`,
          {
            method: "PATCH",

            headers:
              authHeaders(token)
          }
        );

      setInstitutions((items) =>
        items.map((item) =>
          item.id === id
            ? data
            : item
        )
      );

      setMessage(
        "Institution activated successfully."
      );

    } catch (error) {

      setError(error.message);

    }
  }

  return (
    <div className="content">

      <section className="card">

        <div className="card-heading">

          <div>

            <span className="eyebrow">
              ADMINISTRATION
            </span>

            <h3>
              Institutions
            </h3>

            <p>
              Manage institutions registered
              on SecureCert.
            </p>

          </div>

          <strong>
            {institutions.length}
          </strong>

        </div>

        {error && (
          <div className="error">
            {error}
          </div>
        )}

        {message && (
          <div className="success">
            {message}
          </div>
        )}

        {loading ? (

          <div className="empty">
            Loading institutions...
          </div>

        ) : institutions.length === 0 ? (

          <div className="empty">
            No institutions found.
          </div>

        ) : (

          <div className="table-container">

            <table>

              <thead>

                <tr>
                  <th>Institution</th>
                  <th>Registration</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>

              </thead>

              <tbody>

                {institutions.map(
                  (institution) => (

                    <tr
                      key={institution.id}
                    >

                      <td>

                        <strong>
                          {institution.name}
                        </strong>

                        <small>
                          {institution.email}
                        </small>

                      </td>

                      <td>
                        {
                          institution.registrationNumber
                        }
                      </td>

                      <td>

                        <span
                          className={
                            institution.status ===
                            "ACTIVE"
                              ? "status active"
                              : "status pending"
                          }
                        >
                          {institution.status}
                        </span>

                      </td>

                      <td>

                        {institution.status !==
                          "ACTIVE" && (

                          <button
                            className="small-button"
                            onClick={() =>
                              activate(
                                institution.id
                              )
                            }
                          >
                            Activate
                          </button>

                        )}

                      </td>

                    </tr>

                  )
                )}

              </tbody>

            </table>

          </div>

        )}

      </section>

    </div>
  );
}

/* =========================================================
   ADMIN APPROVALS
   ========================================================= */

function AdminApprovals({ token }) {

  const [requests, setRequests] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [message, setMessage] =
    useState("");

  useEffect(() => {

    let cancelled = false;

    async function load() {

      try {

        const data =
          await requestJson(
            `${API}/api/certificate-requests/admin`,
            {
              headers:
                authHeaders(token)
            }
          );

        if (!cancelled) {

          setRequests(
            Array.isArray(data)
              ? data
              : []
          );

        }

      } catch (error) {

        if (!cancelled) {
          setError(error.message);
        }

      } finally {

        if (!cancelled) {
          setLoading(false);
        }

      }
    }

    load();

    return () => {
      cancelled = true;
    };

  }, [token]);

  async function approve(id) {

    setError("");
    setMessage("");

    try {

      const data =
        await requestJson(
          `${API}/api/certificate-requests/${id}/approve`,
          {
            method: "PATCH",

            headers:
              authHeaders(token)
          }
        );

      setRequests((items) =>
        items.filter(
          (item) =>
            item.id !== id
        )
      );

      setMessage(
        data.certificateNumber
          ? `Certificate ${data.certificateNumber} approved successfully.`
          : "Certificate approved successfully."
      );

    } catch (error) {

      setError(error.message);

    }
  }

  async function reject(id) {

    const reason =
      window.prompt(
        "Enter the reason for rejection:"
      );

    if (!reason?.trim()) {
      return;
    }

    setError("");
    setMessage("");

    try {

      await requestJson(
        `${API}/api/certificate-requests/${id}/reject?reason=${encodeURIComponent(
          reason.trim()
        )}`,
        {
          method: "PATCH",

          headers:
            authHeaders(token)
        }
      );

      setRequests((items) =>
        items.filter(
          (item) =>
            item.id !== id
        )
      );

      setMessage(
        "Certificate request rejected."
      );

    } catch (error) {

      setError(error.message);

    }
  }

  return (
    <div className="content">

      <section className="card">

        <div className="card-heading">

          <div>

            <span className="eyebrow">
              FINAL APPROVAL
            </span>

            <h3>
              Certificate Approvals
            </h3>

            <p>
              Review certificate requests
              prepared by the central issuer.
            </p>

          </div>

          <strong>
            {requests.length}
          </strong>

        </div>

        {error && (
          <div className="error">
            {error}
          </div>
        )}

        {message && (
          <div className="success">
            {message}
          </div>
        )}

        {loading ? (

          <div className="empty">
            Loading approval requests...
          </div>

        ) : requests.length === 0 ? (

          <div className="empty">

            <div className="empty-icon">
              ✓
            </div>

            <p>
              No certificate requests are
              waiting for approval.
            </p>

          </div>

        ) : (

          <div className="certificate-list">

            {requests.map(
              (request) => (

                <div
                  className="certificate-item"
                  key={request.id}
                >

                  <div className="certificate-main">

                    <div className="certificate-symbol">
                      !
                    </div>

                    <div>

                      <span className="certificate-number">
                        PENDING ADMIN
                      </span>

                      <h3>
                        {request.courseName}
                      </h3>

                      <p>
                        {request.studentName}
                        {" · "}
                        {request.institutionName}
                      </p>

                      <small>
                        Issuer:{" "}
                        {request.issuerName ||
                          "Not assigned"}
                      </small>

                      {request.description && (
                        <small>
                          {" · "}
                          {request.description}
                        </small>
                      )}

                    </div>

                  </div>

                  <div className="certificate-actions">

                    <span className="status pending">
                      PENDING ADMIN
                    </span>

                    <button
                      className="small-button"
                      onClick={() =>
                        approve(
                          request.id
                        )
                      }
                    >
                      Approve
                    </button>

                    <button
                      className="danger-button"
                      onClick={() =>
                        reject(
                          request.id
                        )
                      }
                    >
                      Reject
                    </button>

                  </div>

                </div>

              )
            )}

          </div>

        )}

      </section>

    </div>
  );
}

/* =========================================================
   PUBLIC VERIFICATION
   ========================================================= */

function VerifyPage({
  initialNumber,
  onBack
}) {
  const [number, setNumber] =
    useState(initialNumber);

  const [result, setResult] =
    useState(null);

  const [error, setError] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  async function verify(event) {

    event.preventDefault();

    setError("");
    setResult(null);
    setLoading(true);

    try {

      const data =
        await requestJson(
          `${API}/api/public/certificates/${encodeURIComponent(
            number.trim()
          )}/verify`
        );

      setResult(data);

    } catch (error) {

      setError(error.message);

    } finally {

      setLoading(false);

    }
  }

  const valid =
    result?.valid === true;

  return (
    <div className="verify-page">

      <header className="verify-header">

        <div
          className="brand"
          onClick={onBack}
        >

          <div className="logo">
            S
          </div>

          <strong>
            SecureCert
          </strong>

        </div>

        <span>
          Public Verification
        </span>

      </header>

      <main className="verify-main">

        <span className="eyebrow">
          TRUSTED CREDENTIALS
        </span>

        <h1>
          Verify a certificate
        </h1>

        <p>
          Verify certificate authenticity,
          integrity and current status.
        </p>

        <form
          className="verify-form"
          onSubmit={verify}
        >

          <input
            value={number}
            placeholder="SC-2026-000001"
            onChange={(event) =>
              setNumber(
                event.target.value
              )
            }
            required
          />

          <button
            type="submit"
            className="primary"
            disabled={loading}
          >
            {loading
              ? "Verifying..."
              : "Verify"}
          </button>

        </form>

        {error && (
          <div className="error">
            {error}
          </div>
        )}

        {result && (

          <section
            className={
              valid
                ? "verification valid"
                : "verification invalid"
            }
          >

            <div className="verification-top">

              <div className="verification-icon">
                {valid ? "✓" : "!"}
              </div>

              <div>

                <span>
                  {valid
                    ? "CERTIFICATE AUTHENTIC"
                    : result.status ===
                      "REVOKED"
                      ? "CERTIFICATE REVOKED"
                      : "CERTIFICATE INVALID"}
                </span>

                <h2>
                  {result.studentName}
                </h2>

                <p>
                  {result.courseName}
                </p>

              </div>

            </div>

            <div className="verification-details">

              <Detail
                label="Certificate"
                value={
                  result.certificateNumber
                }
              />

              <Detail
                label="Institution"
                value={
                  result.institutionName
                }
              />

              <Detail
                label="Issuer"
                value={
                  result.issuerName
                }
              />

              <Detail
                label="Issue Date"
                value={
                  result.issueDate
                }
              />

              <Detail
                label="Integrity"
                value={
                  result.integrityValid
                    ? "✓ Verified"
                    : "✗ Failed"
                }
              />

              <Detail
                label="Digital Signature"
                value={
                  result.signatureValid
                    ? "✓ Verified"
                    : "✗ Failed"
                }
              />

              <Detail
                label="Status"
                value={
                  result.status
                }
              />

            </div>

            {result.status ===
              "REVOKED" &&
              result.revocationReason && (

              <div className="verification-message">

                <strong>
                  Revocation reason
                </strong>

                <p>
                  {result.revocationReason}
                </p>

              </div>

            )}

            {result.message && (
              <div className="verification-message">
                {result.message}
              </div>
            )}

          </section>

        )}

      </main>

    </div>
  );
}

/* =========================================================
   DETAIL
   ========================================================= */

function Detail({
  label,
  value
}) {
  return (
    <div className="detail">

      <span>
        {label}
      </span>

      <strong>
        {value || "—"}
      </strong>

    </div>
  );
}

export default App;