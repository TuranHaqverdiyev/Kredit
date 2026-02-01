import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { loanService } from '../services/loanService';
import { setAccessToken } from '../services/api';
import type { ApiError, LoanResultResponse } from '../types';

function TrackApplication() {
    const [applicationId, setApplicationId] = useState('');
    const [accessToken, setToken] = useState('');
    const [result, setResult] = useState<LoanResultResponse | null>(null);
    const [error, setError] = useState<string | null>(null);

    const trackMutation = useMutation({
        mutationFn: async () => {
            setAccessToken(accessToken);
            return loanService.getResult(applicationId);
        },
        onSuccess: (data) => {
            setResult(data);
            setError(null);
        },
        onError: (err: ApiError) => {
            setError(err.message || 'Müraciət tapılmadı');
            setResult(null);
        },
    });

    const handleTrack = () => {
        if (!applicationId.trim()) {
            setError('Müraciət ID daxil edin');
            return;
        }
        if (!accessToken.trim()) {
            setError('Access token daxil edin');
            return;
        }
        trackMutation.mutate();
    };

    const getDecisionInfo = (decision: string | null) => {
        switch (decision) {
            case 'APPROVED':
                return { icon: '✓', color: 'var(--success-600)', text: 'Təsdiq edildi' };
            case 'REJECTED':
                return { icon: '✕', color: 'var(--error-600)', text: 'Rədd edildi' };
            case 'MANUAL_REVIEW':
                return { icon: '⏳', color: 'var(--warning-500)', text: 'Baxışda' };
            default:
                return { icon: '○', color: 'var(--gray-500)', text: 'Gözləmədə' };
        }
    };

    return (
        <div className="track-container">
            <div className="track-card">
                <h2 className="track-title">Müraciətinizi İzləyin</h2>
                <p className="track-subtitle">
                    Müraciət ID-nizi daxil edərək statusu yoxlayın
                </p>

                {error && <div className="alert alert-error">{error}</div>}

                <div className="form-group">
                    <label className="form-label">Müraciət ID</label>
                    <input
                        type="text"
                        className="form-input"
                        placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                        value={applicationId}
                        onChange={(e) => setApplicationId(e.target.value)}
                    />
                </div>

                <div className="form-group">
                    <label className="form-label">Access Token</label>
                    <input
                        type="text"
                        className="form-input"
                        placeholder="eyJhbGciOiJIUzI1NiIs..."
                        value={accessToken}
                        onChange={(e) => setToken(e.target.value)}
                    />
                    <p style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.5rem' }}>
                        Müraciət zamanı aldığınız token
                    </p>
                </div>

                <button
                    className="btn btn-secondary"
                    style={{ width: '100%' }}
                    onClick={handleTrack}
                    disabled={trackMutation.isPending}
                >
                    {trackMutation.isPending ? 'Yoxlanılır...' : 'Statusu Yoxla'}
                </button>

                {result && (
                    <div style={{ marginTop: '2rem' }}>
                        <div className="result-details">
                            <div className="result-row">
                                <span className="result-label">Status</span>
                                <span className="result-value">{result.status}</span>
                            </div>
                            <div className="result-row">
                                <span className="result-label">Qərar</span>
                                <span
                                    className="result-value"
                                    style={{ color: getDecisionInfo(result.decision).color }}
                                >
                                    {getDecisionInfo(result.decision).icon} {getDecisionInfo(result.decision).text}
                                </span>
                            </div>
                            {result.score && (
                                <div className="result-row">
                                    <span className="result-label">Kredit Balı</span>
                                    <span className="result-value">{result.score}</span>
                                </div>
                            )}
                            {result.approvedAmount && (
                                <div className="result-row">
                                    <span className="result-label">Təsdiq Məbləği</span>
                                    <span className="result-value" style={{ color: 'var(--success-600)' }}>
                                        {result.approvedAmount.toLocaleString()} AZN
                                    </span>
                                </div>
                            )}
                            {result.apr && (
                                <div className="result-row">
                                    <span className="result-label">İllik Faiz</span>
                                    <span className="result-value">{result.apr}%</span>
                                </div>
                            )}
                            <div className="result-row">
                                <span className="result-label">Son yenilənmə</span>
                                <span className="result-value">
                                    {new Date(result.lastUpdated).toLocaleString('az-AZ')}
                                </span>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default TrackApplication;
