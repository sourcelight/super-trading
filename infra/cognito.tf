# ── Cognito user pool (console authentication) ─────────────────────────────────

resource "aws_cognito_user_pool" "main" {
  name                     = "${local.name_prefix}-users"
  auto_verified_attributes = ["email"]
  username_attributes      = ["email"]

  password_policy {
    minimum_length    = 12
    require_lowercase = true
    require_uppercase = true
    require_numbers   = true
    require_symbols   = false
  }
}

resource "aws_cognito_user_pool_domain" "main" {
  domain       = "${local.name_prefix}-${data.aws_caller_identity.current.account_id}"
  user_pool_id = aws_cognito_user_pool.main.id
}

# Public SPA client: Authorization Code + PKCE, no client secret.
resource "aws_cognito_user_pool_client" "spa" {
  name         = "${local.name_prefix}-spa"
  user_pool_id = aws_cognito_user_pool.main.id

  generate_secret                      = false
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = ["openid", "email", "profile"]
  supported_identity_providers         = ["COGNITO"]

  callback_urls = [
    "https://${aws_cloudfront_distribution.main.domain_name}/",
    "http://localhost:5173/",
  ]
  logout_urls = [
    "https://${aws_cloudfront_distribution.main.domain_name}/",
    "http://localhost:5173/",
  ]
}

# ADMIN group → "cognito:groups" claim → ROLE_ADMIN in the backend.
resource "aws_cognito_user_group" "admin" {
  name         = "ADMIN"
  user_pool_id = aws_cognito_user_pool.main.id
  description  = "Console administrators (see all data)"
}
