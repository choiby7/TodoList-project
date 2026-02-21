export interface OAuth2ExchangeResponse {
  success: boolean;
  needsTermsAgreement?: boolean;
  needsAccountMerge?: boolean;
  existingEmail?: string;
  pendingSessionId?: string;
  accessToken?: string;
  refreshToken?: string;
  tokenType?: string;
  expiresIn?: number;
}
