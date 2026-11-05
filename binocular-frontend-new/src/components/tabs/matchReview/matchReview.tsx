import { useEffect, useState } from "react";
import { ApolloClient, gql, HttpLink, InMemoryCache } from "@apollo/client";
import matchReviewStyles from "./matchReview.module.scss";

interface Match {
  matchId: string | null;
  accountLogin: string | null;
  accountName: string | null;
  userGitSignature: string | null;
  matchLayer: string | null;
  confidence: number | null;
  status: string | null;
  signals: string | null;
  reviewedBy: string | null;
  resolvedAt: string | null;
}

const client = new ApolloClient({
  link: new HttpLink({ uri: "/graphQl" }),
  cache: new InMemoryCache(),
  defaultOptions: {
    watchQuery: { fetchPolicy: "no-cache" },
    query: { fetchPolicy: "no-cache" },
  },
});

const MATCHES_QUERY = gql`
  query Matches {
    matches {
      matchId
      accountLogin
      accountName
      userGitSignature
      matchLayer
      confidence
      status
      signals
      reviewedBy
      resolvedAt
    }
  }
`;

const REVIEW_MATCH_MUTATION = gql`
  mutation ReviewMatch($matchId: ID!, $status: String!, $reviewedBy: String!) {
    reviewMatch(matchId: $matchId, status: $status, reviewedBy: $reviewedBy) {
      matchId
      status
      reviewedBy
      resolvedAt
    }
  }
`;

const UNDO_REVIEW_MUTATION = gql`
  mutation UndoReview($matchId: ID!) {
    undoReview(matchId: $matchId) {
      matchId
      status
      reviewedBy
      resolvedAt
    }
  }
`;

function formatTimestamp(value: string | null): string {
  if (!value) return "";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function MatchReview() {
  const [matches, setMatches] = useState<Match[]>([]);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [reviewerName, setReviewerName] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | undefined>(undefined);
  const [pendingMatchId, setPendingMatchId] = useState<string | undefined>(
    undefined,
  );

  async function loadMatches() {
    setLoading(true);
    setError(undefined);
    try {
      const resp = await client.query({ query: MATCHES_QUERY });
      setMatches(resp.data.matches);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load matches");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadMatches();
  }, []);

  async function submitReview(
    matchId: string,
    status: "MANUALLY_CONFIRMED" | "MANUALLY_REJECTED",
  ) {
    if (!reviewerName.trim()) {
      setError("Enter a reviewer name before confirming or rejecting.");
      return;
    }
    setError(undefined);
    setPendingMatchId(matchId);
    try {
      await client.mutate({
        mutation: REVIEW_MATCH_MUTATION,
        variables: { matchId, status, reviewedBy: reviewerName.trim() },
      });
      await loadMatches();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to save review");
    } finally {
      setPendingMatchId(undefined);
    }
  }

  async function submitUndo(matchId: string) {
    setError(undefined);
    setPendingMatchId(matchId);
    try {
      await client.mutate({
        mutation: UNDO_REVIEW_MUTATION,
        variables: { matchId },
      });
      await loadMatches();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to undo review");
    } finally {
      setPendingMatchId(undefined);
    }
  }

  const filteredMatches =
    statusFilter === "ALL"
      ? matches
      : matches.filter((m) => m.status === statusFilter);

  return (
    <div className={"text-xs " + matchReviewStyles.matchReview}>
      <div
        className={
          "border-b border-base-300 pt-1 pb-2 flex gap-2 items-center flex-wrap " +
          matchReviewStyles.toolbar
        }
      >
        <select
          className={"select select-xs " + matchReviewStyles.select}
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="ALL">All statuses</option>
          <option value="PENDING_REVIEW">Pending review</option>
          <option value="LOW_CONFIDENCE">Low confidence</option>
          <option value="AUTO_MATCHED">Auto-matched</option>
          <option value="MANUALLY_CONFIRMED">Manually confirmed</option>
          <option value="MANUALLY_REJECTED">Manually rejected</option>
        </select>
        <input
          className={"input input-xs " + matchReviewStyles.reviewerInput}
          type="text"
          placeholder="your name"
          value={reviewerName}
          onChange={(e) => setReviewerName(e.target.value)}
        />
        <button className={"btn btn-xs"} onClick={() => loadMatches()}>
          Refresh
        </button>
      </div>

      {error && <div className={matchReviewStyles.error}>{error}</div>}

      {loading ? (
        <span className="loading loading-spinner loading-xs text-accent"></span>
      ) : (
        <div className={matchReviewStyles.tableWrapper}>
          <table className={matchReviewStyles.table}>
            <thead>
              <tr>
                <th>Git author</th>
                <th>GitHub account</th>
                <th>Layer</th>
                <th>Confidence</th>
                <th>Status</th>
                <th>Reviewed by</th>
                <th>Resolved at</th>
                <th>Review</th>
              </tr>
            </thead>
            <tbody>
              {filteredMatches.map((m) => {
                const isAutoMatched = m.status === "AUTO_MATCHED";
                const isDecided =
                  m.status === "MANUALLY_CONFIRMED" ||
                  m.status === "MANUALLY_REJECTED";
                const isBusy = pendingMatchId === m.matchId;
                return (
                  <tr key={m.matchId}>
                    <td>{m.userGitSignature}</td>
                    <td>
                      {m.accountLogin}
                      {m.accountName ? ` (${m.accountName})` : ""}
                    </td>
                    <td>{m.matchLayer}</td>
                    <td>{m.confidence?.toFixed(2)}</td>
                    <td>{m.status}</td>
                    <td>{m.reviewedBy || "—"}</td>
                    <td>{formatTimestamp(m.resolvedAt) || "—"}</td>
                    <td>
                      {isAutoMatched ? (
                        <span className={matchReviewStyles.autoLabel}>
                          auto-matched
                        </span>
                      ) : isDecided ? (
                        <button
                          className={"btn btn-xs"}
                          disabled={isBusy}
                          onClick={() => m.matchId && submitUndo(m.matchId)}
                        >
                          Undo
                        </button>
                      ) : (
                        <>
                          <button
                            className={"btn btn-xs join-item"}
                            disabled={isBusy}
                            onClick={() =>
                              m.matchId &&
                              submitReview(m.matchId, "MANUALLY_CONFIRMED")
                            }
                          >
                            Confirm
                          </button>
                          <button
                            className={"btn btn-xs join-item"}
                            disabled={isBusy}
                            onClick={() =>
                              m.matchId &&
                              submitReview(m.matchId, "MANUALLY_REJECTED")
                            }
                          >
                            Reject
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default MatchReview;
